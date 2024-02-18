package me.whizvox.dailyimageposter.reddit;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.exception.UnexpectedResponseException;
import me.whizvox.dailyimageposter.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class RedditClient {

  private static final String
      API_BASE = "https://www.reddit.com/api/v1/",
      OAUTH_BASE = "https://oauth.reddit.com/api/",
      GET = "GET",
      POST = "POST",
      PUT = "PUT",
      DELETE = "DELETE";
  private static final String
      EP_ACCESS_TOKEN = API_BASE + "access_token",
      EP_REVOKE_TOKEN = API_BASE + "revoke_token",
      EP_ME = OAUTH_BASE + "v1/me",
      EP_MEDIA_ASSET = OAUTH_BASE + "media/asset.json";

  private final RedditClientProperties props;
  private final HttpClient client;

  private AccessToken accessToken;

  public RedditClient(RedditClientProperties props) {
    this.props = props;
    client = HttpClient.newBuilder()
        /*.authenticator(new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(props.clientId(), props.clientSecret().toCharArray());
          }
        })*/
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

  private HttpRequest.Builder builder(String uri, String method, Map<String, Object> args, String contentType, String authorization) {
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
    HttpRequest.Builder builder = HttpRequest.newBuilder(actualUri)
        .header("User-Agent", props.userAgent());
    if (argsInBody) {
      if (contentType == null) {
        contentType = "application/x-www-form-urlencoded";
      }
      builder
          .method(method, HttpRequest.BodyPublishers.ofString(encodeUrlParams(args)))
          .header("Content-Type", contentType);
    } else {
      builder.method(method, HttpRequest.BodyPublishers.noBody());
    }
    if (authorization != null) {
      builder.header("Authorization", authorization);
    }
    return builder;
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
  public CompletableFuture<String> uploadImage(Path imageFile, @Nullable String type) {
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
    // TODO Figure out what this returns
    return send(req, s -> s);
  }

  public void saveProperties(Properties props) {
    props.setProperty(DailyImagePoster.PROP_CLIENT_ID, this.props.clientId());
    props.setProperty(DailyImagePoster.PROP_CLIENT_SECRET, this.props.clientSecret());
    props.setProperty(DailyImagePoster.PROP_USERNAME, this.props.username());
    props.setProperty(DailyImagePoster.PROP_PASSWORD, this.props.password());
    props.setProperty(DailyImagePoster.PROP_USER_AGENT, this.props.userAgent());
  }

}
