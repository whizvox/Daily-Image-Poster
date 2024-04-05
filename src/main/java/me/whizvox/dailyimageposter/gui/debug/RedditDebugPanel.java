package me.whizvox.dailyimageposter.gui.debug;

import me.whizvox.dailyimageposter.DailyImagePoster;

import javax.swing.*;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;
import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class RedditDebugPanel extends JPanel {

  public RedditDebugPanel(JFrame parent) {

    JLabel subredditLabel = new JLabel("Subreddit");
    JTextField subredditField = new JTextField();
    JButton subredditButton = new JButton("Fetch subreddit info");
    JLabel linkLabel = new JLabel("Link ID");
    JTextField linkField = new JTextField();
    JButton linkButton = new JButton("Fetch link info");
    JLabel commentLabel = new JLabel("Comment ID");
    JTextField commentField = new JTextField();
    JButton commentButton = new JButton("Fetch comment info");

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(subredditLabel)
        .addComponent(subredditField)
        .addComponent(subredditButton)
        .addComponent(linkLabel)
        .addComponent(linkField)
        .addComponent(linkButton)
        .addComponent(commentLabel)
        .addComponent(commentField)
        .addComponent(commentButton)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(subredditLabel)
        .addComponent(subredditField)
        .addComponent(subredditButton)
        .addGap(GAP_SIZE)
        .addComponent(linkLabel)
        .addComponent(linkField)
        .addComponent(linkButton)
        .addGap(GAP_SIZE)
        .addComponent(commentLabel)
        .addComponent(commentField)
        .addComponent(commentButton)
    );
    setLayout(layout);

    subredditButton.addActionListener(e -> {
      DailyImagePoster.getInstance().getRedditClient().getSubreddit(subredditField.getText())
          .whenComplete((subreddit, ex) -> {
            if (ex == null) {
              LOG.info("Subreddit info: {}", subreddit);
            } else {
              LOG.warn("Could not retrieve subreddit info", ex);
            }
          });
    });
    linkButton.addActionListener(e -> {
      DailyImagePoster.getInstance().getRedditClient().getLink(linkField.getText())
          .whenComplete((link, ex) -> {
            if (ex == null) {
              LOG.info("Link info: {}", link);
            } else {
              LOG.warn("Could not retrieve link info", ex);
            }
          });
    });
    commentButton.addActionListener(e -> {
      DailyImagePoster.getInstance().getRedditClient().getComment(commentField.getText())
          .whenComplete((comment, ex) -> {
            if (ex == null) {
              LOG.info("Comment info: {}", comment);
            } else {
              LOG.warn("Could not retrieve comment info", ex);
            }
          });
    });

  }

}
