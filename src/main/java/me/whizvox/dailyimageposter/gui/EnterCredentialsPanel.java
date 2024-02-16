package me.whizvox.dailyimageposter.gui;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.exception.UnexpectedResponseException;
import me.whizvox.dailyimageposter.reddit.RedditClient;
import me.whizvox.dailyimageposter.reddit.RedditClientProperties;

import javax.swing.*;

public class EnterCredentialsPanel extends JPanel {

  public static final int GAP_SIZE = 10;

  private final JTextField clientIdField;
  private final JTextField clientSecretField;
  private final JTextField usernameField;
  private final JTextField passwordField;

  public EnterCredentialsPanel() {
    JLabel clientIdLabel = new JLabel("Client ID");
    clientIdField = new JTextField();
    JLabel clientSecretLabel = new JLabel("Client Secret");
    clientSecretField = new JPasswordField();
    JLabel usernameLabel = new JLabel("Username");
    usernameField = new JTextField();
    JLabel passwordLabel = new JLabel("Password");
    passwordField = new JPasswordField();
    JButton cancelButton = new JButton("Cancel");
    JButton testButton = new JButton("Test");
    JButton submitButton = new JButton("Submit");

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
        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGap(GAP_SIZE * 4)
            .addComponent(cancelButton)
            .addComponent(testButton)
            .addComponent(submitButton)
        )
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(clientIdLabel)
        .addComponent(clientIdField)
        .addGap(GAP_SIZE)
        .addComponent(clientSecretLabel)
        .addComponent(clientSecretField)
        .addGap(GAP_SIZE)
        .addComponent(usernameLabel)
        .addComponent(usernameField)
        .addGap(GAP_SIZE)
        .addComponent(passwordLabel)
        .addComponent(passwordField)
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(cancelButton)
            .addComponent(testButton)
            .addComponent(submitButton)
        )
    );

    setLayout(layout);

    testButton.addActionListener(event -> testCredentials(client -> {}));
    submitButton.addActionListener(event -> {
      testCredentials(client -> {
        DailyImagePoster app = DailyImagePoster.getInstance();
        app.setClient(client);
        //app.changePanel(new PostPanel(), null);
      });
    });
  }

  private void testCredentials(AccessTokenConsumer onSuccess) {
    String clientId = clientIdField.getText();
    String clientSecret = clientSecretField.getText();
    String username = usernameField.getText();
    String password = passwordField.getText();

    DailyImagePoster app = DailyImagePoster.getInstance();
    RedditClientProperties props = new RedditClientProperties(
        clientId,
        clientSecret,
        app.properties.getProperty(DailyImagePoster.PROP_USER_AGENT),
        username,
        password
    );
    RedditClient client = new RedditClient(props);
    client.fetchAccessToken()
        .thenRun(() -> {
          JOptionPane.showMessageDialog(this, "Success!", "Success", JOptionPane.INFORMATION_MESSAGE);
          onSuccess.accept(client);
        })
        .exceptionally(e -> {
          if (e instanceof UnexpectedResponseException e1) {
            JOptionPane.showMessageDialog(this, "Unexpected response: [" + e1.statusCode() + "] " + e1.body(), "Une1pected response", JOptionPane.WARNING_MESSAGE);
            DailyImagePoster.LOG.warn("Unexpected response", e1);
          } else {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Could not complete request", JOptionPane.WARNING_MESSAGE);
            DailyImagePoster.LOG.warn("Could not complete response", e);
          }
          return null;
        });
    onSuccess.accept(client);
  }

  public interface AccessTokenConsumer {

    void accept(RedditClient client);

  }

}
