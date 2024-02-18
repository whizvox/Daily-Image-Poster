package me.whizvox.dailyimageposter.gui.post;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.db.Post;
import me.whizvox.dailyimageposter.gui.ListPostsPanel;
import me.whizvox.dailyimageposter.reddit.RedditClient;
import me.whizvox.dailyimageposter.util.IOHelper;
import me.whizvox.dailyimageposter.util.StringHelper;
import me.whizvox.dailyimageposter.util.UIHelper;

import javax.imageio.ImageIO;
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
import java.util.List;
import java.util.Objects;
import java.util.TooManyListenersException;
import java.util.UUID;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class PostPanel extends JPanel {

  public static final int IMAGE_SIZE = 128;

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
  private final JLabel noCredentialsLabel;
  private final JButton postButton;

  private UUID id;
  private BufferedImage selectedImage;
  private File lastSelectedDir;
  private int imageWidth, imageHeight;
  private long imageSize;
  private Timer checkRedditClientStatusTimer;

  public PostPanel(PostFrame parent) {
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
    noCredentialsLabel = new JLabel();
    postButton = new JButton("Post");
    selectedImage = null;
    lastSelectedDir = new File(".");
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
    selectImageButton.addActionListener(event -> {
      JFileChooser chooser = new JFileChooser(lastSelectedDir);
      FileNameExtensionFilter filter = new FileNameExtensionFilter("Images (*.png, *.jpeg, *.jpg)", "png", "jpeg", "jpg");
      chooser.setFileFilter(filter);
      int ret = chooser.showOpenDialog(this);
      if (ret == JFileChooser.APPROVE_OPTION) {
        updateImage(chooser.getSelectedFile());
      }
    });
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
              updateImage(droppedFiles.get(0));
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

  private void checkTitle() {
    String title = titleField.getText();
    List<Post> posts = DailyImagePoster.getInstance().getPosts().searchTitle(title);
    if (posts.isEmpty()) {
      JOptionPane.showMessageDialog(this, "No posts found", "No posts found", JOptionPane.INFORMATION_MESSAGE);
    } else {
      JDialog dialog = new JDialog(parent, "Posts found");
      dialog.setContentPane(new ListPostsPanel(posts));
      dialog.setVisible(true);
    }
  }

  private void updateImage(File file) {
    lastSelectedDir = file.getParentFile();
    BufferedImage image;
    try {
      image = ImageIO.read(file);
    } catch (IOException e) {
      DailyImagePoster.LOG.warn("Could not read image", e);
      return;
    }
    imagePreviewLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    selectedImage = image;
    imageWidth = image.getWidth();
    imageHeight = image.getHeight();
    imageSize = file.length();
    imageInfoLabel.setText(StringHelper.formatBytesLength(imageSize) + " | " + imageWidth + "x" + imageHeight);
    UIHelper.updateImageLabel(imagePreviewLabel, image, IMAGE_SIZE);
  }

  private void checkRedditClientStatus() {
    DailyImagePoster app = DailyImagePoster.getInstance();
    RedditClient client = app.getRedditClient();
    if (client == null) {
      noCredentialsLabel.setText("Reddit Credentials are not set! Go to File > Preferences to set them.");
      noCredentialsLabel.setForeground(Color.RED);
      postButton.setEnabled(false);
    } else {
      if (client.hasAccessToken()) {
        noCredentialsLabel.setText("");
        postButton.setEnabled(true);
      } else {
        noCredentialsLabel.setText("Fetching access token...");
        noCredentialsLabel.setForeground(Color.DARK_GRAY);
        postButton.setEnabled(false);
      }
    }
  }

}
