package me.whizvox.dailyimageposter.reddit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.github.mizosoft.methanol.MutableRequest;
import me.whizvox.dailyimageposter.exception.UnexpectedResponseException;
import me.whizvox.dailyimageposter.reddit.pojo.*;
import me.whizvox.dailyimageposter.util.JsonHelper;
import me.whizvox.dailyimageposter.util.StringHelper;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;

public class RedditClient {

  private static final String
      WWW_BASE = "https://www.reddit.com/api/",
      OAUTH_BASE = "https://oauth.reddit.com/",
      OAUTH_API = OAUTH_BASE + "api/",
      TYPE_URLENCODED = "application/x-www-form-urlencoded",
      TYPE_MULTIPART = "multipart/form-data",
      GET = "GET",
      POST = "POST",
      PUT = "PUT",
      DELETE = "DELETE";
  private static final String
      EP_ACCESS_TOKEN = WWW_BASE + "v1/access_token",
      EP_REVOKE_TOKEN = WWW_BASE + "v1/revoke_token",
      EP_ME = OAUTH_API + "v1/me",
      EP_MEDIA_ASSET = OAUTH_API + "media/asset.json",
      EP_SUBMIT = OAUTH_API + "submit",
      EP_INFO = OAUTH_API + "info",
      EP_COMMENT = OAUTH_API + "comment",
      EP_LINK_FLAIRS = OAUTH_BASE + "r/%s/api/link_flair_v2";

  private final RedditClientProperties props;
  private final Methanol client;

  private AccessToken accessToken;
  private LocalDateTime accessTokenExpires;

  public RedditClient(RedditClientProperties props) {
    this.props = props;
    client = Methanol.newBuilder()
        .userAgent(props.userAgent())
        .build();
    accessToken = null;
  }

  private static String encodeUrlParams(Map<String, Object> args) {
    StringBuilder sb = new StringBuilder();
    args.forEach((key, value) -> {
      if (!sb.isEmpty()) {
        sb.append('&');
      }
      sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
      sb.append('=');
      sb.append(URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8));
    });
    return sb.toString();
  }

  private static MultipartBodyPublisher createMultipartPublisher(Map<String, Object> args) {
    var bodyPubBuilder = MultipartBodyPublisher.newBuilder();
    Map<String, Path> files = new HashMap<>();
    args.forEach((key, value) -> {
      if (value instanceof Path path) {
        files.put(key, path);
      } else {
        bodyPubBuilder.textPart(key, value);
      }
    });
    files.forEach((key, path) -> {
      try {
        bodyPubBuilder.filePart(key, path);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("Could not create multipart body publisher", e);
      }
    });
    return bodyPubBuilder.build();
  }

  private String basicAuth() {
    return "Basic " + Base64.getEncoder().encodeToString((props.clientId() + ":" + props.clientSecret()).getBytes());
  }

  private String bearerAuth() {
    if (accessToken == null) {
      throw new IllegalStateException("Cannot perform OAuth request without access token");
    }
    return "Bearer " + accessToken.accessToken;
  }

  public boolean hasAccessToken() {
    return accessToken != null;
  }

  public boolean isAccessTokenExpired() {
    return accessToken != null && accessTokenExpires.isBefore(LocalDateTime.now());
  }

  public boolean accessTokenMatches(String accessToken) {
    return Objects.equals(this.accessToken.accessToken, accessToken);
  }

  private MutableRequest builder(String uri, String method, Map<String, Object> args, String contentType, String authorization) {
    boolean hasArgs = args != null && !args.isEmpty();
    URI actualUri;
    boolean argsInBody;
    if (hasArgs && (method.equalsIgnoreCase(GET) || method.equalsIgnoreCase(DELETE))) {
      actualUri = URI.create(uri + "&" + encodeUrlParams(args));
      argsInBody = false;
    } else {
      actualUri = URI.create(uri);
      argsInBody = hasArgs;
    }
    MutableRequest req = MutableRequest.create(actualUri);
    if (argsInBody) {
      if (contentType == null) {
        contentType = TYPE_URLENCODED;
      }
      if (contentType.equals(TYPE_MULTIPART)) {
        req.method(method, createMultipartPublisher(args));
      } else if (contentType.equals(TYPE_URLENCODED)) {
        req.method(method, HttpRequest.BodyPublishers.ofString(encodeUrlParams(args)));
      } else {
        throw new IllegalArgumentException("Unknown content type: " + contentType);
      }
      req.header("Content-Type", contentType);
    } else {
      req.method(method, HttpRequest.BodyPublishers.noBody());
    }
    if (authorization != null) {
      req.header("Authorization", authorization);
    }
    return req;
  }

  private <T> T handleResponse(HttpResponse<String> response, Function<String, T> func, int maxAttempts, int attempts) {
    if (attempts > maxAttempts) {
      throw new UnexpectedResponseException(response);
    }
    LOG.debug("Response received: {}", StringHelper.responseToString(response));
    if (response.statusCode() / 100 != 2) {
      LOG.warn("Unexpected response received, trying again: {}", StringHelper.responseToString(response));
      return handleResponse(response, func, maxAttempts, attempts + 1);
    }
    return func.apply(response.body());
  }

  private <T> CompletableFuture<T> send(HttpRequest req, Function<String, T> func, boolean checkAccessToken) {
    // update access token if expired
    CompletableFuture<?> first;
    if (checkAccessToken && isAccessTokenExpired()) {
      LOG.info("Access token expired, queueing attempt to retrieve another one");
      first = fetchAccessToken();
    } else {
      first = CompletableFuture.completedFuture(null);
    }
    return first.thenCompose(nil -> {
      LOG.debug("Sending {} request to {}...", req.method(), req.uri());
          return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
              .thenApply(res -> handleResponse(res, func, 3, 0));
        }
    );
  }

  private <T> CompletableFuture<T> send(HttpRequest req, Function<String, T> func) {
    return send(req, func, true);
  }

  private <T> CompletableFuture<T> send(HttpRequest req, Class<T> cls, boolean checkAccessToken) {
    return send(req, res -> JsonHelper.read(res, cls), checkAccessToken);
  }

  private <T> CompletableFuture<T> send(HttpRequest req, Class<T> cls) {
    return send(req, cls, true);
  }

  public void setAccessToken(String accessToken, LocalDateTime expires) {
    if (hasAccessToken()) {
      throw new IllegalStateException("Cannot set access token unless first revoked");
    }
    this.accessToken = new AccessToken();
    this.accessToken.accessToken = accessToken;
    accessTokenExpires = expires;
  }

  public CompletableFuture<AccessToken> fetchAccessToken() {
    Map<String, Object> args = Map.of(
        "grant_type", "password",
        "username", props.username(),
        "password", props.password()
    );
    HttpRequest req = builder(EP_ACCESS_TOKEN, POST, args, null, basicAuth()).build();
    return send(req, AccessToken.class, false)
        .thenApply(accessToken -> {
          this.accessToken = accessToken;
          accessTokenExpires = LocalDateTime.now().plusSeconds(accessToken.expiresIn);
          return accessToken;
        });
  }

  public CompletableFuture<Void> revokeToken() {
    if (accessToken == null) {
      return CompletableFuture.completedFuture(null);
    }
    Map<String, Object> args = Map.of(
        "token", accessToken.accessToken,
        "token_type_hint", "access_token"
    );
    HttpRequest req = builder(EP_REVOKE_TOKEN, POST, args, null, basicAuth()).build();
    return send(req, s -> null)
        .thenRun(() -> this.accessToken = null);
  }

  public CompletableFuture<Me> getMe() {
    HttpRequest req = builder(EP_ME, GET, null, null, bearerAuth()).build();
    return send(req, Me.class);
  }

  // fun fact: this isn't documented anywhere :))))))))))))
  // had to look at PRAW's source code to figure this out

  private static final Map<String, String> IMAGE_TYPES = Map.of(
      "png", "image/png",
      "jpg", "image/jpeg",
      "jpeg", "image/jpeg"
  );
  public CompletableFuture<ImageUploadResult> uploadImage(Path imageFile, @Nullable String type) {
    if (type == null) {
      String fileName = imageFile.getFileName().toString();
      int extIndex = fileName.lastIndexOf('.');
      if (extIndex == -1) {
        throw new IllegalArgumentException("Image file name does not have an extension");
      }
      type = IMAGE_TYPES.get(fileName.substring(extIndex + 1).toLowerCase());
      if (type == null) {
        throw new IllegalArgumentException("No type specified and unknown file extension: " + fileName);
      }
    } else {
      if (IMAGE_TYPES.containsKey(type)) {
        type = IMAGE_TYPES.get(type);
      } else if (!IMAGE_TYPES.containsValue(type)) {
        throw new IllegalArgumentException("Invalid image type: " + type);
      }
    }
    if (!Files.exists(imageFile)) {
      throw new IllegalArgumentException("Image file does not exist: " + imageFile);
    }
    if (!Files.isRegularFile(imageFile)) {
      throw new IllegalArgumentException("Specified path is not a file: " + imageFile);
    }
    HttpRequest req = builder(EP_MEDIA_ASSET, "POST", Map.of(
        "filepath", imageFile.getFileName().toString(),
        "mimetype", type
    ), null, bearerAuth()).build();
    return send(req, UploadMediaLease.class).thenCompose(lease -> {
      LOG.debug("Received media lease from Reddit");
      String url = "https:" + lease.args.action;
      Map<String, Object> args = new HashMap<>();
      lease.args.fields.forEach(entry -> args.put(entry.name, entry.value));
      String imageUrl = url + "/" + args.get("key");
      args.put("file", imageFile);
      HttpRequest req2 = builder(url, POST, args, TYPE_MULTIPART, null).build();
      // response from this is essentially confirmation that the image uploaded. the url that this is uploaded to
      // can be derived from the previous request.
      // also, is key and asset_id the same thing? PRAW returns one or the other depending on the upload_type, but
      // in practice these seem to be the same value
      return send(req2, s -> s).thenApply(s -> new ImageUploadResult(imageUrl, lease.asset.websocketUrl));
    });
  }

  public CompletableFuture<String> submit(SubmitOptions options) {
    Map<String, Object> args = options.toMap();
    HttpRequest req = builder(EP_SUBMIT, POST, args, null, bearerAuth()).build();
    return send(req, s -> s);
  }

  public CompletableFuture<String> uploadAndSubmitImage(Path imageFile, @Nullable String imageType, SubmitOptions options, boolean useWebsocket) {
    // This took me a long time to figure out. Here are the steps to post an image to Reddit:
    // 1) Making a "pre-request" of sorts to set up a later request to AWS
    // 2) Submitting the image to AWS and receiving the image URL and a... websocket URL?
    // 3) Making a request to Reddit to actually post an image submission. The response doesn't include the submission
    //    URL. So how do we get it? Remember that websocket URL?
    // 4) Finally, set up a websocket connection to receive the submission URL
    // the final received JSON looks like this: {"type":"success","payload":{"redirect":"https://www.reddit.com/r/<subreddit>/comments/<submissionId>/<slug>/"}}
    return uploadImage(imageFile, imageType).thenCompose(result -> {
      LOG.debug("Uploaded image: {}", result);
      return submit(options.setUrl(result.imageUrl()).setKind(SubmitOptions.Kind.IMAGE)).thenApply(s -> {
        if (!useWebsocket || result.websocketUrl() == null) {
          return null;
        }
        AtomicReference<String> message = new AtomicReference<>(null);
        // methanol doesn't support websockets
        HttpClient client2 = HttpClient.newBuilder().build();
        WebSocket ws = client2.newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .buildAsync(URI.create(result.websocketUrl()), new WebSocket.Listener() {
              @Override
              public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                String s = data.toString();
                LOG.debug("Text received from websocket: {}", s);
                message.set(s);
                message.notify();
                return WebSocket.Listener.super.onText(webSocket, data, last);
              }}).join();
        // wait until websocket connection receives message
        LOG.debug("Websocket opened");
        synchronized (message) {
          try {
            message.wait(10000);
          } catch (InterruptedException e) {
            LOG.debug("Websocket response thread interrupted", e);
          }
          ws.sendClose(1000, "received data, no longer needed");
          LOG.debug("Websocket closed");
          try {
            String msg = message.get();
            JsonNode root = JsonHelper.OBJECT_MAPPER.readTree(msg);
            if (!root.get("type").asText().equals("success")) {
              throw new RuntimeException("Unexpected websocket response: " + msg);
            }
            return root.get("payload").get("redirect").asText();
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not parse websocket response", e);
          }
        }
      });
    });
  }

  public CompletableFuture<String> submitComment(String fullname, String comment) {
    Map<String, Object> args = Map.of(
        "thing_id", fullname,
        "text", comment
    );
    HttpRequest req = builder(EP_COMMENT, POST, args, null, bearerAuth()).build();
    return send(req, s -> s);
  }

  public CompletableFuture<Link> getLink(String id) {
    HttpRequest req = builder(EP_INFO, GET, Map.of("id", "t3_" + id), null, bearerAuth()).build();
    return send(req, Link.class);
  }

  public CompletableFuture<Comment> getComment(String id) {
    HttpRequest req = builder(EP_INFO, GET, Map.of("id", "t1_" + id), null, bearerAuth()).build();
    return send(req, Comment.class);
  }

  public CompletableFuture<SubredditListing> getSubreddit(String name) {
    HttpRequest req = builder(EP_INFO, GET, Map.of("sr_name", name), null, bearerAuth()).build();
    return send(req, SubredditListing.class);
  }

  public CompletableFuture<List<LinkFlair>> getLinkFlairs(String subreddit) {
    HttpRequest req = builder(EP_LINK_FLAIRS.formatted(subreddit), GET, Map.of(), null, bearerAuth()).build();
    return send(req, s -> {
      try {
        return JsonHelper.OBJECT_MAPPER.readValue(s, new TypeReference<>() {});
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Could not read link flairs", e);
      }
    });
  }

}
