package me.whizvox.dailyimageposter;

import me.whizvox.dailyimageposter.db.BackupManager;
import me.whizvox.dailyimageposter.db.ImageManager;
import me.whizvox.dailyimageposter.db.PostRepository;
import me.whizvox.dailyimageposter.gui.post.PostFrame;
import me.whizvox.dailyimageposter.legacy.ImportLegacyDatabase;
import me.whizvox.dailyimageposter.reddit.RedditClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
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

  public static final int GAP_SIZE = 10;

  public static final Logger LOG = LoggerFactory.getLogger(DailyImagePoster.class);

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
  public final Properties properties;

  private RedditClient client;
  private Connection conn;
  private PostRepository posts;
  private ImageManager imageManager;
  private BackupManager backupManager;

  public DailyImagePoster() {
    properties = new Properties();
    client = null;
    conn = null;
    posts = null;
    imageManager = new ImageManager(Paths.get("images"));
    backupManager = new BackupManager(Paths.get("backups"));
  }

  private void readProperties(String fileName) {
    Path propsFile = Paths.get(fileName);
    if (Files.exists(propsFile)) {
      try (Reader reader = Files.newBufferedReader(Paths.get("./" + fileName))) {
        properties.clear();
        properties.load(reader);
      } catch (IOException e) {
        LOG.warn("Could not read properties file", e);
      }
    }
    properties.clear();
    properties.putAll(createDefaultProperties());
    try (OutputStream out = Files.newOutputStream(propsFile)) {
      properties.store(out, null);
    } catch (IOException e) {
      LOG.warn("Could not create default properties file", e);
    }
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

  public RedditClient getClient() {
    return client;
  }

  public void setClient(RedditClient client) {
    this.client = client;
  }

  public PostRepository getPosts() {
    return posts;
  }

  public ImageManager images() {
    return imageManager;
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
    currentFrame.setVisible(true);
  }

  public void close() {
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
  }

  private static DailyImagePoster instance = null;

  public static DailyImagePoster getInstance() {
    return instance;
  }

  public static void main(String[] args) {
    instance = new DailyImagePoster();
    instance.readProperties("dip.properties");
    try {
      instance.initDatabase("dip.db");
    } catch (SQLException e) {
      throw new RuntimeException("Could not initialize database", e);
    }
    instance.changeFrame(PostFrame::new, null);
    /*instance.currentFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        instance.close();
      }
    });*/
    // import my not-public database schema. this, along with the legacy package will be deleted at some point
    String legacyDirStr = null;
    for (String arg : args) {
      if (arg.startsWith("--legacydir=")) {
        legacyDirStr = arg.substring(12);
        break;
      }
    }
    if (legacyDirStr != null) {
      Path legacyDir = Paths.get(legacyDirStr);
      if (Files.exists(legacyDir) && Files.isDirectory(legacyDir)) {
        LOG.info("Importing legacy database...");
        ImportLegacyDatabase run = new ImportLegacyDatabase();
        run.importLegacy(legacyDir);
      } else {
        LOG.warn("Provided legacy directory is not valid: {}", legacyDirStr);
      }
    }
  }

}