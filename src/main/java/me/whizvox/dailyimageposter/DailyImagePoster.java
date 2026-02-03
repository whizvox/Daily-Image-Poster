package me.whizvox.dailyimageposter;

import dev.brachtendorf.jimagehash.hashAlgorithms.*;
import dev.brachtendorf.jimagehash.hashAlgorithms.experimental.HogHash;
import me.whizvox.dailyimageposter.backup.BackupIntegrityReport;
import me.whizvox.dailyimageposter.backup.BackupRepository;
import me.whizvox.dailyimageposter.backup.BackupService;
import me.whizvox.dailyimageposter.gui.post.CreatePostFrame;
import me.whizvox.dailyimageposter.image.ImageHashRepository;
import me.whizvox.dailyimageposter.image.ImageManager;
import me.whizvox.dailyimageposter.post.PostRepository;
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
      PREF_IMAGE_QUALITY = "image.compressionQuality",
      PREF_MIN_IMAGE_DIMENSION = "image.minDimension",
      PREF_MAX_IMAGE_SIZE = "image.maxSize",
      PREF_LAST_IMAGE_DIRECTORY = "image.lastDirectory",
      PREF_IMAGE_HASH_ALGORITHM = "image.hashAlgorithm",
      PREF_IMAGE_HASH_BIT_RES = "image.hashBitResolution",
      PREF_SIMILARITY_THRESHOLD = "image.similarityThreshold",
      PREF_WAIFU2X_LOCATION = "image.waifu2xLocation",
      PREF_WAIFU2X_ARGS = "image.waifu2xArgs",
      PREF_OPEN_AFTER_POST = "general.openAfterPost",
      PREF_LAST_SELECTED_HISTORY = "legacy.lastSelectedDb";

  private static final Map<String, Object> DEFAULT_PREFERENCES = Map.ofEntries(
      Map.entry(PREF_USER_AGENT, "desktop:me.whizvox.dailyimageposter:v0.1 (by /u/whizvox)"),
      Map.entry(PREF_TITLE_FORMAT, "<title> | Daily Image #<number>"),
      Map.entry(PREF_COMMENT_FORMAT, "* Artist: <artist>\n* Source: <source><if(sourceNsfw)> **(NSFW Warning!)**<endif><if(comment)>\n\n---\n\n<comment><endif>"),
      Map.entry(PREF_IMAGE_QUALITY, 90),
      Map.entry(PREF_MIN_IMAGE_DIMENSION, 750),
      Map.entry(PREF_MAX_IMAGE_SIZE, 1_100_000),
      Map.entry(PREF_IMAGE_HASH_ALGORITHM, "perceptive"),
      Map.entry(PREF_IMAGE_HASH_BIT_RES, 32),
      Map.entry(PREF_SIMILARITY_THRESHOLD, 0.15),
      Map.entry(PREF_WAIFU2X_ARGS, "-c 9 -q 90"),
      Map.entry(PREF_OPEN_AFTER_POST, true)
  );

  public final Preferences preferences;
  private final DIPArguments arguments;
  private final Path tempDir;
  private JFrame currentFrame;
  private RedditClient client;
  private Connection conn;
  private PostRepository posts;
  private ImageManager imageManager;
  private BackupRepository backupRepo;
  private BackupService backupService;
  private ImageHashRepository hashRepo;

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
    imageManager = null;
    backupRepo = null;
    backupService = null;
    hashRepo = null;
  }

  private HashingAlgorithm getHashingAlgorithm() {
    int res = preferences.getInt(PREF_IMAGE_HASH_BIT_RES);
    String algorithm = preferences.getString(PREF_IMAGE_HASH_ALGORITHM);
    return switch (algorithm) {
      case "perceptive" -> new PerceptiveHash(res);
      case "average" -> new AverageHash(res);
      case "averageColor" -> new AverageColorHash(res);
      case "difference" -> new DifferenceHash(res, DifferenceHash.Precision.Simple);
      case "wavelet" -> new WaveletHash(res, 3);
      case "median" -> new MedianHash(res);
      case "averageKernel" -> new AverageKernelHash(res);
      case "rotAverage" -> new RotAverageHash(res);
      case "rotP" -> new RotPHash(res);
      case "hog" -> //noinspection deprecation
          new HogHash(res);
      default -> throw new RuntimeException("Unknown hashing algorithm: " + algorithm);
    };
  }

  private void initDatabase(String dbName) throws SQLException {
    conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
    backupRepo = new BackupRepository(conn);
    backupRepo.create();
    backupService = new BackupService(Paths.get("backups"), backupRepo);
    Path dbPath = Paths.get(dbName);
    if (Files.exists(dbPath)) {
      backupService.createBackup(dbPath);
    }
    BackupIntegrityReport report = backupService.verifyIntegrity(true, true);
    report.log();
    backupService.cleanupOldBackups(dbName, 20);
    posts = new PostRepository(conn);
    posts.create();
    hashRepo = new ImageHashRepository(conn);
    hashRepo.create();
    imageManager = new ImageManager(Paths.get("images"), getHashingAlgorithm(), hashRepo);
  }

  @Nullable
  public RedditClient getRedditClient() {
    return client;
  }

  public void onPreferencesUpdated() {
    imageManager.setHashingAlgorithm(getHashingAlgorithm());
    if (arguments.noReddit) {
      LOG.debug("Reddit client disabled, so it will not be updated");
      return;
    }
    // attempt to re-use previously stored access token
    String accessToken = preferences.getString(PREF_ACCESS_TOKEN);
    LocalDateTime accessTokenExpires = preferences.getDateTime(PREF_ACCESS_TOKEN_EXPIRES);
    if (accessTokenExpires.isBefore(LocalDateTime.now())) {
      accessToken = null;
      accessTokenExpires = null;
    }
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

  public PostRepository posts() {
    return posts;
  }

  public ImageManager images() {
    return imageManager;
  }

  public ImageHashRepository hashes() {
    return hashRepo;
  }

  private void close() {
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
    arguments.parse(args);
    instance = new DailyImagePoster(arguments);
    instance.preferences.load();
    try {
      instance.initDatabase("dip.db");
    } catch (SQLException e) {
      throw new RuntimeException("Could not initialize database", e);
    }
    instance.onPreferencesUpdated();
    instance.changeFrame(CreatePostFrame::new, null);
  }

}