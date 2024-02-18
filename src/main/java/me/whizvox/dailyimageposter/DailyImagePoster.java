package me.whizvox.dailyimageposter;

import me.whizvox.dailyimageposter.db.BackupManager;
import me.whizvox.dailyimageposter.db.ImageManager;
import me.whizvox.dailyimageposter.db.PostRepository;
import me.whizvox.dailyimageposter.gui.post.PostFrame;
import me.whizvox.dailyimageposter.legacy.ImportLegacyDatabase;
import me.whizvox.dailyimageposter.reddit.RedditClient;
import me.whizvox.dailyimageposter.reddit.RedditClientProperties;
import me.whizvox.dailyimageposter.util.StringHelper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.function.Supplier;

public class DailyImagePoster {

  public static final Logger LOG = LoggerFactory.getLogger("DailyImagePoster");

  public static final String
      PROP_CLIENT_ID = "client.clientId",
      PROP_CLIENT_SECRET = "client.clientSecret",
      PROP_USERNAME = "client.username",
      PROP_PASSWORD = "client.password",
      PROP_USER_AGENT = "client.userAgent";

  private static Properties createDefaultProperties() {
    Properties props = new Properties();
    props.put(PROP_CLIENT_ID, "");
    props.put(PROP_CLIENT_SECRET, "");
    props.put(PROP_USERNAME, "");
    props.put(PROP_PASSWORD, "");
    props.put(PROP_USER_AGENT, "DailyImagePoster Script by whizvox");
    return props;
  }

  private JFrame currentFrame;
  public final Properties preferences;
  private final Path prefsFile;

  private RedditClient client;
  private Connection conn;
  private PostRepository posts;
  private ImageManager imageManager;
  private BackupManager backupManager;

  public DailyImagePoster() {
    preferences = new Properties();
    client = null;
    conn = null;
    posts = null;
    prefsFile = Paths.get("dip.properties");
    imageManager = new ImageManager(Paths.get("images"));
    backupManager = new BackupManager(Paths.get("backups"));
  }

  private void loadPreferences() {
    if (Files.exists(prefsFile)) {
      try (Reader reader = Files.newBufferedReader(prefsFile)) {
        preferences.clear();
        preferences.load(reader);
      } catch (IOException e) {
        LOG.warn("Could not read properties file", e);
      }
    } else {
      preferences.clear();
      preferences.putAll(createDefaultProperties());
      savePreferences();
    }
  }

  public void savePreferences() {
    try (OutputStream out = Files.newOutputStream(prefsFile)) {
      preferences.store(out, null);
    } catch (IOException e) {
      LOG.warn("Could not create default properties file", e);
    }
    LOG.info("Preferences saved");
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
    if (client != null && client.hasAccessToken()) {
      client.revokeToken();
    }
    client = null;
    RedditClientProperties props = new RedditClientProperties(
        preferences.getProperty(PROP_CLIENT_ID),
        preferences.getProperty(PROP_CLIENT_SECRET),
        preferences.getProperty(PROP_USER_AGENT),
        preferences.getProperty(PROP_USERNAME),
        preferences.getProperty(PROP_PASSWORD)
    );
    if (StringHelper.isNullOrBlank(props.clientId()) || StringHelper.isNullOrBlank(props.clientSecret()) ||
        StringHelper.isNullOrBlank(props.userAgent()) || StringHelper.isNullOrBlank(props.username()) ||
        StringHelper.isNullOrBlank(props.password())) {
      LOG.info("Reddit client not updated, not all credentials fields are set");
    } else {
      client = new RedditClient(props);
      client.fetchAccessToken()
          .thenRun(() -> LOG.info("Access token successfully retrieved"))
          .exceptionally(e -> {
            LOG.warn("Could not retrieve access token", e);
            return null;
          });
    }
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
    if (client != null && client.hasAccessToken()) {
      client.revokeToken().thenRun(() -> {
        LOG.info("Access token successfully revoked");
      }).exceptionally(e -> {
        LOG.warn("Could not revoke access token", e);
        return null;
      }).join(); // wait for thread to finish
    }
  }

  public void changeFrame(Supplier<JFrame> frameSupplier, String title) {
    if (currentFrame != null) {
      currentFrame.dispose();
    }
    currentFrame = frameSupplier.get();
    currentFrame.setLocationRelativeTo(null);
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
    currentFrame.setVisible(true);
  }

  private static DailyImagePoster instance = null;

  public static DailyImagePoster getInstance() {
    return instance;
  }

  public static void main(String[] args) {
    instance = new DailyImagePoster();
    instance.loadPreferences();
    instance.updateRedditClient();
    try {
      instance.initDatabase("dip.db");
    } catch (SQLException e) {
      throw new RuntimeException("Could not initialize database", e);
    }
    instance.changeFrame(PostFrame::new, null);
    // import my not-public database schema. this, along with the legacy package will be deleted at some point
    String legacyDirStr = null;
    boolean addAllLegacyFiles = false;
    for (String arg : args) {
      if (arg.startsWith("--legacydir=")) {
        legacyDirStr = arg.substring(12);
      }
      if (arg.equals("--legacyaddall")) {
        addAllLegacyFiles = true;
      }
    }
    if (legacyDirStr != null) {
      Path legacyDir = Paths.get(legacyDirStr);
      if (Files.exists(legacyDir) && Files.isDirectory(legacyDir)) {
        LOG.info("Importing legacy database...");
        ImportLegacyDatabase run = new ImportLegacyDatabase();
        run.importLegacy(legacyDir, !addAllLegacyFiles);
      } else {
        LOG.warn("Provided legacy directory is not valid: {}", legacyDirStr);
      }
    }
  }

}