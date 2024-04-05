package me.whizvox.dailyimageposter.gui;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.post.Post;
import me.whizvox.dailyimageposter.util.IOHelper;
import me.whizvox.dailyimageposter.util.UIHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.StrokeBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;
import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class ViewPostPanel extends JPanel {

  private static JLabel createValueLabel(Object value, boolean monospacedFont) {
    JLabel label = value == null ? new JLabel("<none>") : new JLabel(String.valueOf(value));
    if (value == null) {
      label.setForeground(Color.GRAY);
    }
    if (monospacedFont) {
      label.setFont(Font.decode(Font.MONOSPACED));
    } else {
      label.setFont(label.getFont().deriveFont(Font.PLAIN));
    }
    return label;
  }

  public ViewPostPanel(Post post) {
    JLabel idLabel = new JLabel("ID");
    JLabel idValue = createValueLabel(post.id(), true);
    JLabel numberLabel = new JLabel("Number");
    JLabel numberValue = createValueLabel(post.formatNumber(), true);
    JLabel titleLabel = new JLabel("Title");
    JLabel titleValue = createValueLabel(post.title(), false);
    JLabel artistLabel = new JLabel("Artist");
    JLabel artistValue = createValueLabel(post.artist(), false);
    JLabel sourceLabel = new JLabel("Source");
    JLabel sourceValue = createValueLabel(post.source(), false);
    if (post.source() != null) {
      UIHelper.addHyperlink(sourceValue, post.source());
    }
    JLabel commentLabel = new JLabel("Comment");
    JTextArea commentValue = new JTextArea(Objects.requireNonNullElse(post.comment(), ""));
    commentValue.setEnabled(false);
    commentValue.setForeground(Color.BLACK);
    JLabel imageNsfwLabel = new JLabel("Is image NSFW?");
    JLabel imageNsfwValue = createValueLabel(post.imageNsfw() ? "Yes" : "No", false);
    if (post.imageNsfw()) {
      imageNsfwValue.setForeground(Color.ORANGE);
    }
    JLabel sourceNsfwLabel = new JLabel("Is source NSFW?");
    JLabel sourceNsfwValue = createValueLabel(post.sourceNsfw() ? "Yes" : "No", false);
    if (post.sourceNsfw()) {
      sourceNsfwValue.setForeground(Color.ORANGE);
    }
    JLabel imagePreview = new JLabel();
    imagePreview.setBorder(new StrokeBorder(new BasicStroke(1), Color.BLACK));
    try {
      BufferedImage image;
      Path imagePath = DailyImagePoster.getInstance().images().getImagePath(post);
      if (imagePath == null) {
        LOG.warn("Could not open image: {}", post.fileName());
        image = ImageIO.read(IOHelper.getResource("noimage.png"));
      } else {
        image = ImageIO.read(imagePath.toFile());
      }
      UIHelper.updateImageLabel(imagePreview, image, 128);
    } catch (IOException e) {
      LOG.warn("Could not read image", e);
    }
    JLabel redditPostIdLabel = new JLabel("Reddit Post ID");
    JLabel redditPostIdValue = createValueLabel(post.redditPostId(), true);
    JButton redditPostLink = new JButton("Open");
    JLabel redditCommentIdLabel = new JLabel("Reddit Comment ID");
    JLabel redditCommentIdValue = createValueLabel(post.redditCommentId(), true);
    JButton redditCommentLink = new JButton("Open");
    JLabel imgurIdLabel = new JLabel("Imgur ID");
    JLabel imgurIdValue = createValueLabel(post.imgurId(), true);
    JButton imgurLink = new JButton("Open");
    JLabel whenPostedLabel = new JLabel("When posted");
    JLabel whenPostedValue = createValueLabel(Optional.ofNullable(post.whenPosted()).map(DateTimeFormatter.ISO_DATE_TIME::format).orElse(null), false);

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addComponent(idLabel)
            .addComponent(idValue)
            .addComponent(numberLabel)
            .addComponent(numberValue)
            .addComponent(titleLabel)
            .addComponent(titleValue)
            .addComponent(artistLabel)
            .addComponent(artistValue)
            .addComponent(sourceLabel)
            .addComponent(sourceValue, 10, 50, 100)
            .addComponent(commentLabel)
            .addComponent(commentValue, 100, 200, 400)
            .addComponent(imageNsfwLabel)
            .addComponent(imageNsfwValue)
            .addComponent(sourceNsfwLabel)
            .addComponent(sourceNsfwValue)
        )
        .addGroup(layout.createParallelGroup()
            .addComponent(imagePreview, GroupLayout.Alignment.CENTER)
            .addComponent(redditPostIdLabel)
            .addGroup(layout.createSequentialGroup()
                .addComponent(redditPostIdValue)
                .addComponent(redditPostLink)
            )
            .addComponent(redditCommentIdLabel)
            .addGroup(layout.createSequentialGroup()
                .addComponent(redditCommentIdValue)
                .addComponent(redditCommentLink)
            )
            .addComponent(imgurIdLabel)
            .addGroup(layout.createSequentialGroup()
                .addComponent(imgurIdValue)
                .addComponent(imgurLink)
            )
            .addComponent(whenPostedLabel)
            .addComponent(whenPostedValue)
        )
    );
    layout.setVerticalGroup(layout.createParallelGroup()
        .addGroup(layout.createSequentialGroup()
            .addComponent(idLabel)
            .addComponent(idValue)
            .addGap(GAP_SIZE)
            .addComponent(numberLabel)
            .addComponent(numberValue)
            .addGap(GAP_SIZE)
            .addComponent(titleLabel)
            .addComponent(titleValue)
            .addGap(GAP_SIZE)
            .addComponent(artistLabel)
            .addComponent(artistValue)
            .addGap(GAP_SIZE)
            .addComponent(sourceLabel)
            .addComponent(sourceValue)
            .addGap(GAP_SIZE)
            .addComponent(commentLabel)
            .addComponent(commentValue, 24, 24, 100)
            .addGap(GAP_SIZE)
            .addComponent(imageNsfwLabel)
            .addComponent(imageNsfwValue)
            .addGap(GAP_SIZE)
            .addComponent(sourceNsfwLabel)
            .addComponent(sourceNsfwValue)
        )
        .addGroup(layout.createSequentialGroup()
            .addComponent(imagePreview)
            .addGap(GAP_SIZE)
            .addComponent(redditPostIdLabel)
            .addGroup(layout.createParallelGroup()
                .addComponent(redditPostIdValue)
                .addComponent(redditPostLink)
            )
            .addGap(GAP_SIZE)
            .addComponent(redditCommentIdLabel)
            .addGroup(layout.createParallelGroup()
                .addComponent(redditCommentIdValue)
                .addComponent(redditCommentLink)
            )
            .addGap(GAP_SIZE)
            .addComponent(imgurIdLabel)
            .addGroup(layout.createParallelGroup()
                .addComponent(imgurIdValue)
                .addComponent(imgurLink)
            )
            .addGap(GAP_SIZE)
            .addComponent(whenPostedLabel)
            .addComponent(whenPostedValue)
        )
    );
    setLayout(layout);

    if (post.redditPostId() == null) {
      redditPostLink.setEnabled(false);
    } else {
      redditPostLink.addActionListener(event -> UIHelper.browse("https://redd.it/" + post.redditPostId()));
    }
    redditCommentLink.setEnabled(false);
    if (post.imgurId() == null) {
      imgurLink.setEnabled(false);
    } else {
      imgurLink.addActionListener(event -> UIHelper.browse("https://imgur.com/" + post.imgurId()));
    }

  }

}
