package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.gui.DocumentChangedListener;
import me.whizvox.dailyimageposter.reddit.RedditClient;
import me.whizvox.dailyimageposter.reddit.RedditClientProperties;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class CredentialsPrefsPanel extends AbstractPrefsPanel {

  private final JTextField clientIdField;
  private final JPasswordField clientSecretField;
  private final JTextField usernameField;
  private final JPasswordField passwordField;
  private final JTextField userAgentField;

  public CredentialsPrefsPanel(PreferencesPanel parent) {
    super(parent);

    DailyImagePoster app = DailyImagePoster.getInstance();
    JLabel clientIdLabel = new JLabel("Client ID");
    clientIdField = new JTextField(app.preferences.getProperty(DailyImagePoster.PROP_CLIENT_ID));
    JLabel clientSecretLabel = new JLabel("Client Secret");
    clientSecretField = new JPasswordField(app.preferences.getProperty(DailyImagePoster.PROP_CLIENT_SECRET));
    JLabel usernameLabel = new JLabel("Username");
    usernameField = new JTextField(app.preferences.getProperty(DailyImagePoster.PROP_USERNAME));
    JLabel passwordLabel = new JLabel("Password");
    passwordField = new JPasswordField(app.preferences.getProperty(DailyImagePoster.PROP_PASSWORD));
    JLabel userAgentLabel = new JLabel("User Agent");
    userAgentField = new JTextField(app.preferences.getProperty(DailyImagePoster.PROP_USER_AGENT));
    JCheckBox showSecretBox = new JCheckBox("Reveal secret fields");
    JButton testButton = new JButton("Test");
    JLabel testResultLabel = new JLabel("");

    char echoChar = clientSecretField.getEchoChar();
    int fh = clientIdField.getPreferredSize().height;

        GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup());
    setLayout(layout);
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
        .addComponent(testButton, GroupLayout.Alignment.TRAILING)
        .addComponent(testResultLabel, GroupLayout.Alignment.CENTER)
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
    );
    setLayout(layout);

    clientIdField.getDocument().addDocumentListener((DocumentChangedListener) event -> parent.markUnsavedChanges());
    clientSecretField.getDocument().addDocumentListener((DocumentChangedListener) event -> parent.markUnsavedChanges());
    usernameField.getDocument().addDocumentListener((DocumentChangedListener) event -> parent.markUnsavedChanges());
    passwordField.getDocument().addDocumentListener((DocumentChangedListener) event -> parent.markUnsavedChanges());
    userAgentField.getDocument().addDocumentListener((DocumentChangedListener) event -> parent.markUnsavedChanges());

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
  }

  @Override
  public void saveChanges(Properties props) {
    props.setProperty(DailyImagePoster.PROP_CLIENT_ID, clientIdField.getText());
    props.setProperty(DailyImagePoster.PROP_CLIENT_SECRET, clientSecretField.getText());
    props.setProperty(DailyImagePoster.PROP_USERNAME, usernameField.getText());
    props.setProperty(DailyImagePoster.PROP_PASSWORD, passwordField.getText());
    props.setProperty(DailyImagePoster.PROP_USER_AGENT, userAgentField.getText());
  }

}
