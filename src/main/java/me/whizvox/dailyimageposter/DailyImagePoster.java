package me.whizvox.dailyimageposter;

import me.whizvox.dailyimageposter.db.BackupManager;
import me.whizvox.dailyimageposter.db.PostRepository;
import me.whizvox.dailyimageposter.gui.MainFrame;
import me.whizvox.dailyimageposter.gui.PostPanel;
import me.whizvox.dailyimageposter.reddit.RedditClient;
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

public class DailyImagePoster {

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

  public final MainFrame mainFrame;
  public final Properties properties;

  private RedditClient client;
  private Connection conn;
  private PostRepository posts;
  private BackupManager backupManager;

  public DailyImagePoster() {
    mainFrame = new MainFrame();
    properties = new Properties();
    client = null;
    conn = null;
    posts = null;
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

  public void changePanel(JPanel panel, String title) {
    mainFrame.update(panel, title);
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
    instance.mainFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        instance.close();
      }
    });
    instance.changePanel(new PostPanel(), null);
    // import my not-public database schema. this, along with the legacy package will be deleted at some point
    /*ImportLegacyDatabase run = new ImportLegacyDatabase();
    run.importLegacy(Paths.get("C:\\Users\\corne\\Pictures\\Daily Haruhiism\\history.json"));*/
  }

}