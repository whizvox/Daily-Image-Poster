package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.reddit.RedditClient;
import me.whizvox.dailyimageposter.reddit.RedditClientProperties;
import me.whizvox.dailyimageposter.reddit.pojo.LinkFlair;
import me.whizvox.dailyimageposter.util.StringHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class RedditPrefsPanel extends AbstractPrefsPanel {

  private final JTextField clientIdField;
  private final JPasswordField clientSecretField;
  private final JTextField usernameField;
  private final JPasswordField passwordField;
  private final JTextField userAgentField;

  private final JTextField subredditField;
  private final JTextField flairIdField;
  private final JButton selectFlairButton;
  private final JTextField flairTextField;
  private final JTextField titleFormatField;
  private final JTextArea commentFormatField;

  public RedditPrefsPanel(PreferencesPanel parent) {
    super(parent);

    DailyImagePoster app = DailyImagePoster.getInstance();
    JLabel clientIdLabel = new JLabel("Client ID");
    clientIdField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_CLIENT_ID));
    JLabel clientSecretLabel = new JLabel("Client Secret");
    clientSecretField = new JPasswordField(app.preferences.getString(DailyImagePoster.PREF_CLIENT_SECRET));
    JLabel usernameLabel = new JLabel("Username");
    usernameField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_USERNAME));
    JLabel passwordLabel = new JLabel("Password");
    passwordField = new JPasswordField(app.preferences.getString(DailyImagePoster.PREF_PASSWORD));
    JLabel userAgentLabel = new JLabel("User Agent");
    userAgentField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_USER_AGENT));
    JCheckBox showSecretBox = new JCheckBox("Reveal secret fields");
    JButton testButton = new JButton("Test");
    JLabel testResultLabel = new JLabel("");
    JSeparator separator = new JSeparator();
    JLabel subredditLabel = new JLabel("Subreddit Name");
    subredditField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_SUBREDDIT_NAME));
    JLabel flairIdLabel = new JLabel("Flair ID");
    flairIdField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_FLAIR_ID));

    JLabel flairTextLabel = new JLabel("Flair Text");
    flairTextField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_FLAIR_TEXT));
    selectFlairButton = new JButton("Select");
    JLabel titleFormatLabel = new JLabel("Title Format");
    titleFormatField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_TITLE_FORMAT));
    JLabel commentFormatLabel = new JLabel("Comment Format");
    commentFormatField = new JTextArea(app.preferences.getString(DailyImagePoster.PREF_COMMENT_FORMAT), 5, 50);
    commentFormatField.setFont(Font.decode(Font.MONOSPACED));

    char echoChar = clientSecretField.getEchoChar();
    int fh = clientIdField.getPreferredSize().height;

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(clientIdLabel)
        .addComponent(clientIdField)
        .addComponent(clientSecretLabel)
        .addComponent(clientSecretField)
        .addComponent(usernameLabel)
        .addComponent(usernameField)
        .addComponent(passwordLabel)
        .addComponent(passwordField)
        .addComponent(userAgentLabel)
        .addComponent(userAgentField)
        .addComponent(showSecretBox)
        .addComponent(testButton)
        .addComponent(testResultLabel)
        .addComponent(separator)
        .addComponent(subredditLabel)
        .addComponent(subredditField)
        .addComponent(flairIdLabel)
        .addGroup(layout.createSequentialGroup()
            .addComponent(flairIdField)
            .addComponent(selectFlairButton)
        )
        .addComponent(flairTextLabel)
        .addComponent(flairTextField)
        .addComponent(titleFormatLabel)
        .addComponent(titleFormatField)
        .addComponent(commentFormatLabel)
        .addComponent(commentFormatField)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(clientIdLabel)
        .addComponent(clientIdField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(clientSecretLabel)
        .addComponent(clientSecretField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(usernameLabel)
        .addComponent(usernameField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(passwordLabel)
        .addComponent(passwordField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(userAgentLabel)
        .addComponent(userAgentField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(showSecretBox)
        .addComponent(testButton)
        .addComponent(testResultLabel)
        .addComponent(separator)
        .addComponent(subredditLabel)
        .addComponent(subredditField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(flairIdLabel)
        .addGroup(layout.createParallelGroup()
            .addComponent(flairIdField, fh, fh, fh)
            .addComponent(selectFlairButton, fh, fh, fh)
        )
        .addGap(GAP_SIZE)
        .addComponent(flairTextLabel)
        .addComponent(flairTextField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(titleFormatLabel)
        .addComponent(titleFormatField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(commentFormatLabel)
        .addComponent(commentFormatField)
    );
    setLayout(layout);

    parent.addChangeListeners(
        clientIdField, clientSecretField, usernameField, userAgentField, subredditField, flairIdField, flairTextField,
        titleFormatField, commentFormatField
    );

    showSecretBox.addChangeListener(event -> {
      if (showSecretBox.isSelected()) {
        clientSecretField.setEchoChar((char) 0);
        passwordField.setEchoChar((char) 0);
      } else {
        clientSecretField.setEchoChar(echoChar);
        passwordField.setEchoChar(echoChar);
      }
    });
    testButton.addActionListener(event -> {
      testButton.setEnabled(false);
      RedditClient client = new RedditClient(new RedditClientProperties(
          clientIdField.getText(),
          new String(clientSecretField.getPassword()),
          userAgentField.getText(),
          usernameField.getText(),
          new String(passwordField.getPassword())
      ));
      testResultLabel.setText("Initiating test...");
      testResultLabel.setForeground(Color.BLACK);
      client.fetchAccessToken().thenRun(() -> {
        testResultLabel.setText("Access token successfully retrieved");
        testResultLabel.setForeground(Color.CYAN);
        client.getMe().thenAccept(me -> {
          testResultLabel.setText("Test request successfully completed");
          testResultLabel.setForeground(Color.CYAN);
        }).exceptionally(throwable -> {
          testResultLabel.setText("Test request failed!");
          DailyImagePoster.LOG.warn("Test request failed", throwable);
          testResultLabel.setForeground(Color.RED);
          return null;
        }).whenComplete((unused, throwable) -> {
          client.revokeToken().thenRun(() -> {
            testResultLabel.setText("Test successful!");
            testResultLabel.setForeground(Color.GREEN);
          }).exceptionally(throwable2 -> {
            testResultLabel.setText("Revoking token failed!");
            DailyImagePoster.LOG.warn("Token revocation failed", throwable2);
            testResultLabel.setForeground(Color.RED);
            return null;
          });
        });
      }).exceptionally(throwable -> {
        testResultLabel.setText("Access token fetch failed: " + throwable.getMessage());
        DailyImagePoster.LOG.warn("Access token fetch failed", throwable);
        testResultLabel.setForeground(Color.RED);
        return null;
      }).whenComplete((unused, throwable) -> {
        testButton.setEnabled(true);
      });
    });
    selectFlairButton.addActionListener(e -> selectFlair());
  }

  private void selectFlair() {
    String subreddit = subredditField.getText();
    if (StringHelper.isNullOrBlank(subreddit)) {
      JOptionPane.showMessageDialog(this, "You must first specify a subreddit");
    } else {
      LinkFlair flair = RedditLinkFlairsDialog.selectFlair(null, subreddit);
      if (flair != null) {
        flairIdField.setText(flair.id);
        if (flair.textEditable) {
          flairTextField.setText(flair.text);
        }
      }
    }
  }

  @Override
  public void saveChanges(Map<String, Object> prefs) {
    prefs.put(DailyImagePoster.PREF_CLIENT_ID, clientIdField.getText());
    prefs.put(DailyImagePoster.PREF_CLIENT_SECRET, new String(clientSecretField.getPassword()));
    prefs.put(DailyImagePoster.PREF_USERNAME, usernameField.getText());
    prefs.put(DailyImagePoster.PREF_PASSWORD, new String(passwordField.getPassword()));
    prefs.put(DailyImagePoster.PREF_USER_AGENT, userAgentField.getText());
    prefs.put(DailyImagePoster.PREF_SUBREDDIT_NAME, subredditField.getText());
    prefs.put(DailyImagePoster.PREF_FLAIR_ID, flairIdField.getText());
    prefs.put(DailyImagePoster.PREF_FLAIR_TEXT, flairTextField.getText());
    prefs.put(DailyImagePoster.PREF_TITLE_FORMAT, titleFormatField.getText());
    prefs.put(DailyImagePoster.PREF_COMMENT_FORMAT, commentFormatField.getText());
  }

}
