package me.whizvox.dailyimageposter;

import me.whizvox.dailyimageposter.db.BackupManager;
import me.whizvox.dailyimageposter.db.ImageManager;
import me.whizvox.dailyimageposter.db.PostRepository;
import me.whizvox.dailyimageposter.gui.post.PostFrame;
import me.whizvox.dailyimageposter.reddit.RedditClient;
import me.whizvox.dailyimageposter.reddit.RedditClientProperties;
import me.whizvox.dailyimageposter.util.Preferences;
import me.whizvox.dailyimageposter.util.StringHelper;
import me.whizvox.dailyimageposter.util.UIHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

public class DailyImagePoster {

  public static final Logger LOG = LoggerFactory.getLogger("DailyImagePoster");

  public static final String
      PREF_ACCESS_TOKEN = "client.accessToken",
      PREF_ACCESS_TOKEN_EXPIRES = "client.accessTokenExpires",
      PREF_CLIENT_ID = "client.clientId",
      PREF_CLIENT_SECRET = "client.clientSecret",
      PREF_USERNAME = "client.username",
      PREF_PASSWORD = "client.password",
      PREF_USER_AGENT = "client.userAgent",
      PREF_SUBREDDIT_NAME = "reddit.subreddit",
      PREF_TITLE_FORMAT = "reddit.titleFormat",
      PREF_COMMENT_FORMAT = "reddit.commentFormat",
      PREF_FLAIR_ID = "reddit.flairId",
      PREF_FLAIR_TEXT = "reddit.flairText",
      PREF_IMAGE_QUALITY = "general.imageCompressionQuality",
      PREF_MIN_IMAGE_DIMENSION = "general.imageMinDimension",
      PREF_MAX_IMAGE_SIZE = "general.imageMaxSize",
      PREF_LAST_SELECTED_HISTORY = "legacy.lastSelectedDb";

  private static final Map<String, Object> DEFAULT_PREFERENCES = Map.of(
      PREF_USER_AGENT, "desktop:me.whizvox.dailyimageposter:v0.1 (by /u/whizvox)",
      PREF_TITLE_FORMAT, "<title> | Daily Image #<number>",
      PREF_COMMENT_FORMAT, "Artist: <artist>\nSource: <source><if(sourceNsfw)> **(NSFW Warning!)**<endif>\n<if(comment)>\n---\n<comment><endif>",
      PREF_IMAGE_QUALITY, 90,
      PREF_MIN_IMAGE_DIMENSION, 750,
      PREF_MAX_IMAGE_SIZE, 1_100_000
  );

  private final DIPArguments arguments;
  private JFrame currentFrame;
  public final Preferences preferences;
  private final Path tempDir;

  private RedditClient client;
  private Connection conn;
  private PostRepository posts;
  private ImageManager imageManager;
  private BackupManager backupManager;

  public DailyImagePoster(DIPArguments arguments) {
    this.arguments = arguments;
    preferences = new Preferences(Paths.get("dip.properties"), DEFAULT_PREFERENCES);
    client = null;
    conn = null;
    posts = null;
    tempDir = Paths.get("temp");
    try {
      Files.createDirectories(tempDir);
    } catch (IOException e) {
      throw new RuntimeException("Could not create temp directory", e);
    }
    imageManager = new ImageManager(Paths.get("images"));
    backupManager = new BackupManager(Paths.get("backups"));
  }

  private void initDatabase(String dbName) throws SQLException {
    Path dbPath = Paths.get(dbName);
    if (Files.exists(dbPath)) {
      try {
        backupManager.createBackup(dbPath, false);
      } catch (IOException e) {
        LOG.warn("Could not create backup of database " + dbName, e);
      }
    }
    conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
    posts = new PostRepository(conn);
    posts.create();
  }

  @Nullable
  public RedditClient getRedditClient() {
    return client;
  }

  public void updateRedditClient() {
    if (arguments.noReddit) {
      LOG.debug("Reddit client disabled, so it will not be updated");
      return;
    }
    // attempt to re-use previously stored access token
    String accessToken = preferences.getString(PREF_ACCESS_TOKEN);
    LocalDateTime accessTokenExpires = preferences.getDateTime(PREF_ACCESS_TOKEN_EXPIRES);
    if (client != null && client.hasAccessToken()) {
      if (client.isAccessTokenExpired()) {
        client.revokeToken();
        accessToken = null;
      } else if (!client.accessTokenMatches(accessToken)) {
        accessToken = null;
      }
    }
    client = null;
    RedditClientProperties props = new RedditClientProperties(
        preferences.getString(PREF_CLIENT_ID),
        preferences.getString(PREF_CLIENT_SECRET),
        preferences.getString(PREF_USER_AGENT),
        preferences.getString(PREF_USERNAME),
        preferences.getString(PREF_PASSWORD)
    );
    if (StringHelper.isNullOrBlank(props.clientId()) || StringHelper.isNullOrBlank(props.clientSecret()) ||
        StringHelper.isNullOrBlank(props.userAgent()) || StringHelper.isNullOrBlank(props.username()) ||
        StringHelper.isNullOrBlank(props.password())) {
      LOG.info("Reddit client not updated, not all credentials fields are set");
    } else {
      client = new RedditClient(props);
      if (accessToken == null) {
        LOG.info("Fetching access token from Reddit");
        client.fetchAccessToken()
            .thenAccept(token -> {
              preferences.setString(PREF_ACCESS_TOKEN, token.accessToken);
              // assume, at most, 5 seconds passed since the token has been granted
              preferences.setDateTime(PREF_ACCESS_TOKEN_EXPIRES, LocalDateTime.now().plusSeconds(token.expiresIn - 5));
              preferences.save();
              LOG.info("Access token successfully retrieved");
            })
            .exceptionally(e -> {
              LOG.warn("Could not retrieve access token", e);
              return null;
            });
      } else {
        LOG.debug("Re-using previous Reddit access token");
        client.setAccessToken(accessToken, accessTokenExpires);
      }
    }
  }

  public Path getTempPath(String fileName) {
    return tempDir.resolve(fileName);
  }

  public PostRepository getPosts() {
    return posts;
  }

  public ImageManager images() {
    return imageManager;
  }

  private void close() {
    try {
      backupManager.saveMetaData();
    } catch (IOException e) {
      LOG.warn("Could not save backup metadata", e);
    }
    try {
      conn.close();
    } catch (SQLException e) {
      LOG.warn("Could not close database connection", e);
    }
    if (arguments.autoRevokeRedditToken && client != null && client.hasAccessToken()) {
      client.revokeToken().thenRun(() -> {
        LOG.info("Access token successfully revoked");
      }).exceptionally(e -> {
        LOG.warn("Could not revoke access token", e);
        return null;
      }).join(); // wait for thread to finish
    }
    preferences.save();
  }

  public void changeFrame(Supplier<JFrame> frameSupplier, String title) {
    if (currentFrame != null) {
      currentFrame.dispose();
    }
    currentFrame = frameSupplier.get();
    currentFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    if (title == null) {
      currentFrame.setTitle("Daily Image Poster");
    } else {
      currentFrame.setTitle(title + " | Daily Image Poster");
    }
    currentFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        // don't close resources unless window has been closed by user
        close();
      }
    });
    UIHelper.addMenuBar(currentFrame);
    currentFrame.pack();
    currentFrame.setLocationRelativeTo(null);
    currentFrame.setVisible(true);
  }

  private static DailyImagePoster instance = null;

  public static DailyImagePoster getInstance() {
    return instance;
  }

  public static void main(String[] args) {
    DIPArguments arguments = new DIPArguments();
    for (String arg : args) {
      if (arg.equals("--noreddit")) {
        arguments.noReddit = true;
      }
      if (arg.equals("--autorevokereddit")) {
        arguments.autoRevokeRedditToken = true;
      }
    }

    instance = new DailyImagePoster(arguments);
    instance.preferences.load();
    instance.updateRedditClient();
    try {
      instance.initDatabase("dip.db");
    } catch (SQLException e) {
      throw new RuntimeException("Could not initialize database", e);
    }
    instance.changeFrame(PostFrame::new, null);

  }

}