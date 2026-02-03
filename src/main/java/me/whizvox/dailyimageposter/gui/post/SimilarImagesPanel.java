package me.whizvox.dailyimageposter.gui.post;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.image.ImageManager;
import me.whizvox.dailyimageposter.post.Post;
import me.whizvox.dailyimageposter.util.UIHelper;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.StrokeBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class SimilarImagesPanel extends JPanel {

  private final JLabel pageLabel;
  private final JLabel similarityLabel;
  private final JLabel imageLabel;
  private final JLabel titleLabel;

  private final List<ImageManager.SimilarImage> images;
  private final Runnable postUpdate;
  private int selectedImageIndex;

  public SimilarImagesPanel(List<ImageManager.SimilarImage> images, @Nullable Runnable postUpdate) throws HeadlessException {
    assert !images.isEmpty() : "Cannot initialize a similar images frame with no images!";
    this.images = images;
    this.postUpdate = Objects.requireNonNullElse(postUpdate, () -> {});

    JButton nextButton = new JButton("Next >>");
    JButton prevButton = new JButton("<< Previous");
    pageLabel = new JLabel("(1/?)");
    similarityLabel = new JLabel("Similarity: ?%");
    imageLabel = new JLabel();
    imageLabel.setBorder(new StrokeBorder(new BasicStroke(1), Color.BLACK));
    titleLabel = new JLabel("Loading...");

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addGroup(layout.createSequentialGroup()
            .addComponent(pageLabel)
            .addComponent(similarityLabel)
        )
        .addComponent(imageLabel, GroupLayout.Alignment.CENTER)
        .addComponent(titleLabel, GroupLayout.Alignment.CENTER)
        .addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
            .addComponent(prevButton)
            .addComponent(nextButton)
        )
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addComponent(pageLabel, GroupLayout.Alignment.LEADING)
            .addComponent(similarityLabel, GroupLayout.Alignment.TRAILING)
        )
        .addComponent(imageLabel)
        .addComponent(titleLabel)
        .addGroup(layout.createParallelGroup()
            .addComponent(prevButton)
            .addComponent(nextButton)
        )
    );
    setLayout(layout);

    if (images.size() == 1) {
      nextButton.setEnabled(false);
      prevButton.setEnabled(false);
    } else {
      nextButton.addActionListener(e -> {
        if (selectedImageIndex >= images.size() - 1) {
          selectedImageIndex = 0;
        } else {
          selectedImageIndex++;
        }
        updateDetails();
      });
      prevButton.addActionListener(e -> {
        if (selectedImageIndex <= 0) {
          selectedImageIndex = images.size() - 1;
        } else {
          selectedImageIndex--;
        }
        updateDetails();
      });
    }

    SwingUtilities.invokeLater(this::updateDetails);
  }

  private void updateDetails() {
    ImageManager.SimilarImage image = images.get(selectedImageIndex);
    pageLabel.setText("(%d/%d)".formatted(selectedImageIndex + 1, images.size()));
    similarityLabel.setText("Similarity: %.1f%%".formatted((1 - image.similarity()) * 100));
    Path path = DailyImagePoster.getInstance().images().getImagePath(image.fileName());
    BufferedImage bufImg = null;
    try (InputStream in = Files.newInputStream(path)) {
      bufImg = ImageIO.read(in);
    } catch (IOException e) {
      DailyImagePoster.LOG.error("Could not open image {}", path, e);
    }
    if (bufImg != null) {
      UIHelper.updateImageLabel(imageLabel, bufImg, 600);
    }
    Post post = DailyImagePoster.getInstance().posts().getByFileName(image.fileName());
    if (post != null) {
      titleLabel.setText("[%s] %s".formatted(post.formatNumber(), post.title()));
    } else {
      titleLabel.setText("<unknown post>");
    }
    postUpdate.run();
  }

}
