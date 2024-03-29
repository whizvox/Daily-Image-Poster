package me.whizvox.dailyimageposter.gui.post;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.db.ImageManager;
import me.whizvox.dailyimageposter.db.Post;
import me.whizvox.dailyimageposter.gui.ListPostsPanel;
import me.whizvox.dailyimageposter.reddit.RedditClient;
import me.whizvox.dailyimageposter.reddit.SubmitOptions;
import me.whizvox.dailyimageposter.util.IOHelper;
import me.whizvox.dailyimageposter.util.StringHelper;
import me.whizvox.dailyimageposter.util.UIHelper;
import org.stringtemplate.v4.ST;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import javax.swing.border.StrokeBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class PostPanel extends JPanel {

  public static final int IMAGE_SIZE = 128;

  private final DailyImagePoster app;
  private final PostFrame parent;
  private final JLabel idLabel;
  private final JTextField numberField;
  private final JButton latestNumberButton;
  private final JButton checkNumberButton;
  private final JTextField titleField;
  private final JButton checkTitleButton;
  private final JTextField artistField;
  private final JTextField sourceField;
  private final JButton checkSourceButton;
  private final JTextField commentField;
  private final JCheckBox postNsfwBox;
  private final JCheckBox sourceNsfwBox;
  private final JLabel imagePreviewLabel;
  private final JLabel imageInfoLabel;
  private final JButton selectImageButton;
  private final JButton shrinkImageButton;
  private final JButton upscaleImageButton;
  private final JButton findSimilarButton;
  private final JLabel noCredentialsLabel;
  private final JButton postButton;

  private UUID id;
  private BufferedImage selectedImage;
  private Path selectedImageFile;
  private Path lastSelectedDir;
  private int imageWidth, imageHeight;
  private long imageSize;
  private Timer checkRedditClientStatusTimer;

  public PostPanel(PostFrame parent) {
    app = DailyImagePoster.getInstance();
    this.parent = parent;
    id = UUID.randomUUID();
    idLabel = new JLabel("ID: " + id);
    JLabel numberLabel = new JLabel("Number");
    numberField = new JTextField();
    latestNumberButton = new JButton("Latest");
    checkNumberButton = new JButton("Check");
    JLabel titleLabel = new JLabel("Title");
    titleField = new JTextField();
    checkTitleButton = new JButton("Check");
    JLabel artistLabel = new JLabel("Artist");
    artistField = new JTextField();
    JLabel sourceLabel = new JLabel("Source");
    sourceField = new JTextField();
    checkSourceButton = new JButton("Check");
    JLabel commentLabel = new JLabel("Comment");
    commentField = new JTextField();
    postNsfwBox = new JCheckBox("Is post NSFW?");
    sourceNsfwBox = new JCheckBox("Is source NSFW?");
    BufferedImage img;
    try {
      img = ImageIO.read(IOHelper.getResource("noimage.png"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    imagePreviewLabel = new JLabel(new ImageIcon(img.getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_FAST)));
    imagePreviewLabel.setBorder(new StrokeBorder(new BasicStroke(1), Color.BLACK));
    imageInfoLabel = new JLabel("No image selected", SwingConstants.CENTER);
    selectImageButton = new JButton("Select image...");
    shrinkImageButton = new JButton("Shrink");
    upscaleImageButton = new JButton("Upscale");
    findSimilarButton = new JButton("Find similar");
    noCredentialsLabel = new JLabel(" ");
    postButton = new JButton("Post");
    selectedImage = null;
    selectedImageFile = null;
    lastSelectedDir = null;
    imageWidth = imageHeight = 0;
    imageSize = 0L;

    final int FH = checkNumberButton.getPreferredSize().height;

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup()
                .addComponent(numberLabel)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(numberField, 100, 100, 400)
                    .addComponent(latestNumberButton)
                    .addComponent(checkNumberButton)
                )
                .addComponent(titleLabel)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(titleField, 100, 100, 400)
                    .addComponent(checkTitleButton)
                )
                .addComponent(artistLabel)
                .addComponent(artistField, 100, 100, 400)
                .addComponent(sourceLabel)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(sourceField, 100, 200, 400)
                    .addComponent(checkSourceButton)
                )
                .addComponent(commentLabel)
                .addComponent(commentField, 100, 200, 400)
                .addComponent(postNsfwBox)
                .addComponent(sourceNsfwBox)
            )
            .addGap(GAP_SIZE)
            .addGroup(layout.createParallelGroup()
                .addComponent(imagePreviewLabel, GroupLayout.Alignment.CENTER)
                .addComponent(imageInfoLabel, GroupLayout.Alignment.CENTER)
                .addComponent(selectImageButton, GroupLayout.Alignment.CENTER)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(shrinkImageButton)
                    .addComponent(upscaleImageButton)
                )
                .addComponent(findSimilarButton, GroupLayout.Alignment.CENTER)
            )
        )
        .addComponent(noCredentialsLabel, GroupLayout.Alignment.CENTER)
        .addComponent(postButton, GroupLayout.Alignment.TRAILING)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addComponent(numberLabel)
                .addGroup(layout.createParallelGroup()
                    .addComponent(numberField, FH, FH, FH)
                    .addComponent(latestNumberButton)
                    .addComponent(checkNumberButton)
                )
                .addGap(GAP_SIZE)
                .addComponent(titleLabel)
                .addGroup(layout.createParallelGroup()
                    .addComponent(titleField, FH, FH, FH)
                    .addComponent(checkTitleButton)
                )
                .addGap(GAP_SIZE)
                .addComponent(artistLabel)
                .addComponent(artistField, FH, FH, FH)
                .addGap(GAP_SIZE)
                .addComponent(sourceLabel)
                .addGroup(layout.createParallelGroup()
                    .addComponent(sourceField, FH, FH, FH)
                    .addComponent(checkSourceButton)
                )
                .addGap(GAP_SIZE)
                .addComponent(commentLabel)
                .addComponent(commentField, FH, FH, FH)
                .addGap(GAP_SIZE)
                .addComponent(postNsfwBox)
                .addGap(GAP_SIZE)
                .addComponent(sourceNsfwBox)
            )
            .addGroup(layout.createSequentialGroup()
                .addComponent(imagePreviewLabel)
                .addComponent(imageInfoLabel)
                .addGap(GAP_SIZE)
                .addComponent(selectImageButton)
                .addGap(GAP_SIZE)
                .addGroup(layout.createParallelGroup()
                    .addComponent(shrinkImageButton)
                    .addComponent(upscaleImageButton)
                )
                .addGap(GAP_SIZE)
                .addComponent(findSimilarButton)
            )
        )
        .addGap(GAP_SIZE)
        .addComponent(noCredentialsLabel)
        .addComponent(postButton)
    );
    setLayout(layout);

    checkTitleButton.addActionListener(event -> checkTitle());
    imagePreviewLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (selectedImage != null) {
          JDialog dialog = new JDialog(parent, "Image preview");
          JLabel imageLabel = new JLabel(new ImageIcon(selectedImage));
          dialog.add(imageLabel);
          dialog.pack();
          dialog.setVisible(true);
        }
      }
    });
    selectImageButton.addActionListener(event -> selectImage());
    shrinkImageButton.addActionListener(event -> {
      if (imageSize > 0 && imageSize < app.preferences.getInt(DailyImagePoster.PREF_MAX_IMAGE_SIZE)) {
        int ret = JOptionPane.showConfirmDialog(this, "Are you sure you wish to shrink this image? It's already within an expected size.", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ret != JOptionPane.YES_OPTION) {
          return;
        }
        shrinkImage();
      }
    });
    findSimilarButton.addActionListener(event -> findSimilarImages());
    postButton.addActionListener(event -> postImage());
    checkRedditClientStatusTimer = new Timer(1000, e -> checkRedditClientStatus());
    checkRedditClientStatusTimer.start();

    setDropTarget(new DropTarget());
    try {
      getDropTarget().addDropTargetListener(new DropTargetListener() {
        @Override public void dragEnter(DropTargetDragEvent dtde) {}
        @Override public void dragOver(DropTargetDragEvent dtde) {}
        @Override public void dropActionChanged(DropTargetDragEvent dtde) {}
        @Override public void dragExit(DropTargetEvent dte) {}

        @Override
        public void drop(DropTargetDropEvent event) {
          event.acceptDrop(DnDConstants.ACTION_COPY);
          try {
            List<File> droppedFiles = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (!droppedFiles.isEmpty()) {
              updateImage(droppedFiles.get(0).toPath());
            }
          } catch (UnsupportedFlavorException | IOException e) {
            DailyImagePoster.LOG.warn("Could not perform drag-and-drop operation", e);
          }
        }
      });
    // why is this a checked exception????
    } catch (TooManyListenersException e) {
      throw new RuntimeException(e);
    }
  }

  public void updatePost(Post post) {
    idLabel.setText("ID: " + post.id());
    numberField.setText(post.formatNumber());
    titleField.setText(Objects.requireNonNullElse(post.title(), ""));
    artistField.setText(Objects.requireNonNullElse(post.artist(), ""));
    sourceField.setText(Objects.requireNonNullElse(post.source(), ""));
    commentField.setText(Objects.requireNonNullElse(post.comment(), ""));
    postNsfwBox.setSelected(post.imageNsfw());
    sourceNsfwBox.setSelected(post.sourceNsfw());
    boolean enablePostButton = post.whenPosted() == null;
    postButton.setEnabled(enablePostButton);
  }

  private void selectImage() {
    Path dir;
    if (selectedImageFile == null) {
      dir = Paths.get(Objects.requireNonNullElse(app.preferences.getString(DailyImagePoster.PREF_LAST_IMAGE_DIRECTORY), "."));
    } else {
      dir = selectedImageFile.getParent();
    }
    JFileChooser chooser = new JFileChooser(dir.toFile());
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.png, *.jpeg, *.jpg)", "png", "jpeg", "jpg");
    chooser.setFileFilter(filter);
    int ret = chooser.showOpenDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
      updateImage(chooser.getSelectedFile().toPath());
    }
  }

  private void checkTitle() {
    String title = titleField.getText();
    List<Post> posts = app.posts().searchTitle(title);
    if (posts.isEmpty()) {
      JOptionPane.showMessageDialog(this, "No posts found", "No posts found", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JDialog dialog = new JDialog(parent, "Posts found");
      dialog.setContentPane(new ListPostsPanel(posts));
      dialog.setVisible(true);
    }
  }

  private void updateImage(Path imagePath) {
    selectedImageFile = imagePath;
    lastSelectedDir = selectedImageFile.getParent();
    app.preferences.setString(DailyImagePoster.PREF_LAST_IMAGE_DIRECTORY, lastSelectedDir.toAbsolutePath().normalize().toString());
    BufferedImage image;
    try (InputStream in = Files.newInputStream(selectedImageFile)) {
      image = ImageIO.read(in);
    } catch (IOException e) {
      DailyImagePoster.LOG.warn("Could not read image", e);
      return;
    }
    imagePreviewLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    selectedImage = image;
    imageWidth = image.getWidth();
    imageHeight = image.getHeight();
    try {
      imageSize = Files.size(imagePath);
    } catch (IOException e) {
      DailyImagePoster.LOG.warn("Could not get file size for " + imagePath, e);
      imageSize = 0;
    }
    imageInfoLabel.setText(StringHelper.formatBytesLength(imageSize) + " | " + imageWidth + "x" + imageHeight);
    UIHelper.updateImageLabel(imagePreviewLabel, image, IMAGE_SIZE);
  }

  private void shrinkImage() {
    if (selectedImage == null) {
      JOptionPane.showMessageDialog(this, "No image selected", null, JOptionPane.WARNING_MESSAGE);
      return;
    }
    String[] parts = StringHelper.getFileNameBaseAndExtension(selectedImageFile.getFileName().toString());
    Path newPath = app.getTempPath(parts[0] + "_compressed" + parts[1]);
    int copyNumber = 0;
    while (Files.exists(newPath)) {
      copyNumber++;
      newPath = app.getTempPath(parts[0] + "_compressed (" + copyNumber + ")" + parts[1]);
    }
    long origSize = imageSize;
    int origWidth = selectedImage.getWidth();
    Path origLastSelectedDir = lastSelectedDir;
    int maxSize = app.preferences.getInt(DailyImagePoster.PREF_MAX_IMAGE_SIZE);
    float quality = app.preferences.getInt(DailyImagePoster.PREF_IMAGE_QUALITY) / 100.0F;
    long currentImageSize;
    float currentWidth = imageWidth;
    float currentHeight = imageHeight;
    while (true) {
      try {
        IOHelper.saveAsJpeg(selectedImage, newPath, (int) currentWidth, (int) currentHeight, quality);
        currentImageSize = Files.size(newPath);
        DailyImagePoster.LOG.debug("Image shrink progress: path={}, size={}, dimensions={}x{}", newPath, currentImageSize, currentWidth, currentHeight);
        if (currentImageSize > maxSize) {
          currentWidth *= 0.95F;
          currentHeight *= 0.95F;
          Files.delete(newPath);
        } else {
          break;
        }
      } catch (IOException e) {
        DailyImagePoster.LOG.warn("Could not shrink image", e);
      }
    }
    updateImage(newPath);
    lastSelectedDir = origLastSelectedDir;
    double sizeDiff = (imageSize - origSize) / (double) origSize;
    double dimensionDiff = (imageWidth - origWidth) / (double) origWidth;
    JOptionPane.showMessageDialog(this, "Image shrunk to %s (%d%%) and %dx%d (%d%%)".formatted(
        StringHelper.formatBytesLength(imageSize), (int) (sizeDiff * 100), imageWidth, imageHeight,
        (int) (dimensionDiff * 100))
    );
  }

  private void findSimilarImages() {
    if (selectedImageFile == null) {
      return;
    }
    findSimilarButton.setText("Working...");
    findSimilarButton.setEnabled(false);
    SwingUtilities.invokeLater(() -> {
      List<ImageManager.SimilarImage> similarImages = app.images().findSimilarImages(
          selectedImageFile, app.preferences.getDouble(DailyImagePoster.PREF_SIMILARITY_THRESHOLD));
      similarImages.sort(Comparator.comparingDouble(ImageManager.SimilarImage::similarity));
      findSimilarButton.setText("Find similar");
      findSimilarButton.setEnabled(true);
      if (similarImages.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No similar images found!");
      } else {
        JOptionPane.showMessageDialog(this, "This image has been found " + similarImages.size() + " time(s):\n" +
            similarImages.stream()
                .map(i -> "[%.3f] %s".formatted(i.similarity(), i.fileName())).collect(Collectors.joining("\n")));
      }
    });
  }

  private void checkRedditClientStatus() {
    RedditClient client = app.getRedditClient();
    if (client == null) {
      noCredentialsLabel.setText("Reddit Credentials are not set! Go to File > Preferences to set them.");
      noCredentialsLabel.setForeground(Color.RED);
      postButton.setEnabled(false);
    } else {
      if (client.hasAccessToken()) {
        noCredentialsLabel.setText(" ");
        postButton.setEnabled(true);
      } else {
        noCredentialsLabel.setText("Fetching access token...");
        noCredentialsLabel.setForeground(Color.DARK_GRAY);
        postButton.setEnabled(false);
      }
    }
  }

  private void postImage() {
    if (selectedImage != null) {
      int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to submit this?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (response == JOptionPane.YES_OPTION) {
        RedditClient client = app.getRedditClient();
        if (client != null) {
          Map<String, Object> args = Map.of(
              "number", numberField.getText(),
              "title", titleField.getText(),
              "artist", artistField.getText(),
              "source", sourceField.getText(),
              "comment", commentField.getText(),
              "postNsfw", postNsfwBox.isSelected(),
              "sourceNsfw", sourceNsfwBox.isSelected()
          );
          String subreddit = app.preferences.getString(DailyImagePoster.PREF_SUBREDDIT_NAME);
          ST titleTemplate = new ST(app.preferences.getString(DailyImagePoster.PREF_TITLE_FORMAT));
          args.forEach(titleTemplate::add);
          String title = titleTemplate.render();
          ST commentTemplate = new ST(app.preferences.getString(DailyImagePoster.PREF_COMMENT_FORMAT));
          args.forEach(commentTemplate::add);
          String comment = commentTemplate.render();
          client.uploadAndSubmitImage(selectedImageFile, null, new SubmitOptions()
              .setSubreddit(subreddit)
              .setNsfw(postNsfwBox.isSelected())
              //.setFlairId()
              .setTitle(title), true).whenComplete((submissionUrl, ex) -> {
            if (ex == null) {
              DailyImagePoster.LOG.info("Link submission successful: {}", submissionUrl);
              String linkId = StringHelper.getRedditLinkId(submissionUrl);
              client.submitComment("t3_" + linkId, comment).whenComplete((s, ex2) -> {
                if (ex2 == null) {
                  DailyImagePoster.LOG.info("Comment submission successful: {}", s);
                } else {
                  DailyImagePoster.LOG.warn("Comment post unsuccessful", ex2);
                }
              });
            } else {
              DailyImagePoster.LOG.warn("Upload unsuccessful", ex);
            }
          });
        }
      }
    }
  }

}
