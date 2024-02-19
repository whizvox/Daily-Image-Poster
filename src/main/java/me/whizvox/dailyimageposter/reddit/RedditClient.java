package me.whizvox.dailyimageposter.reddit;

import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.github.mizosoft.methanol.MutableRequest;
import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.exception.UnexpectedResponseException;
import me.whizvox.dailyimageposter.util.JsonHelper;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class RedditClient {

  private static final String
      WWW_BASE = "https://www.reddit.com/api/",
      OAUTH_BASE = "https://oauth.reddit.com/api/",
      TYPE_URLENCODED = "application/x-www-form-urlencoded",
      TYPE_MULTIPART = "multipart/form-data",
      GET = "GET",
      POST = "POST",
      PUT = "PUT",
      DELETE = "DELETE";
  private static final String
      EP_ACCESS_TOKEN = WWW_BASE + "v1/access_token",
      EP_REVOKE_TOKEN = WWW_BASE + "v1/revoke_token",
      EP_ME = OAUTH_BASE + "v1/me",
      EP_MEDIA_ASSET = OAUTH_BASE + "media/asset.json",
      EP_SUBMIT = OAUTH_BASE + "submit";

  private final RedditClientProperties props;
  private final Methanol client;

  private AccessToken accessToken;

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

  private <T> CompletableFuture<T> send(HttpRequest req, Function<String, T> func) {
    return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
        .thenApply(res -> {
          if (res.statusCode() / 100 != 2) {
            throw new UnexpectedResponseException(res);
          }
          return func.apply(res.body());
        });
  }

  private <T> CompletableFuture<T> send(HttpRequest req, Class<T> cls) {
    return send(req, res -> JsonHelper.read(res, cls));
  }

  public CompletableFuture<?> fetchAccessToken() {
    Map<String, Object> args = Map.of(
        "grant_type", "password",
        "username", props.username(),
        "password", props.password()
    );
    HttpRequest req = builder(EP_ACCESS_TOKEN, POST, args, null, basicAuth()).build();
    return send(req, AccessToken.class)
        .thenAccept(accessToken -> this.accessToken = accessToken);
  }

  public CompletableFuture<?> revokeToken() {
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
      type = IMAGE_TYPES.get(fileName.substring(extIndex + 1));
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
      DailyImagePoster.LOG.debug("Received media lease from Reddit");
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
      DailyImagePoster.LOG.debug("Uploaded image: {}", result);
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
                DailyImagePoster.LOG.debug("Text received from websocket: {}", s);
                message.set(s);
                message.notify();
                return WebSocket.Listener.super.onText(webSocket, data, last);
              }}).join();
        // wait until websocket connection receives message
        DailyImagePoster.LOG.debug("Websocket opened");
        synchronized (message) {
          try {
            message.wait(10000);
          } catch (InterruptedException e) {
            DailyImagePoster.LOG.debug("Websocket response thread interrupted", e);
          }
          ws.sendClose(1000, "received data, no longer needed");
          DailyImagePoster.LOG.debug("Websocket closed");
          return message.get();
        }
      });
    });
  }

}
