package me.whizvox.dailyimageposter.gui.reserve;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.gui.DocumentChangedListener;
import me.whizvox.dailyimageposter.gui.post.SimilarImagesPanel;
import me.whizvox.dailyimageposter.image.ImageManager;
import me.whizvox.dailyimageposter.reserve.Reserve;
import me.whizvox.dailyimageposter.util.IOHelper;
import me.whizvox.dailyimageposter.util.StringHelper;
import me.whizvox.dailyimageposter.util.UIHelper;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.StrokeBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.TooManyListenersException;
import java.util.UUID;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;
import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class EditReservePanel extends JPanel {

  private static final int IMAGE_SIZE = 128;

  private final Window parent;
  private UUID editId;
  private Path selectedImageFile;
  private Path lastSelectedDir;
  private boolean imageUpdated;
  private boolean modified;

  private final JLabel titleLabel;
  private final JTextField titleField;
  private final JLabel artistLabel;
  private final JTextField artistField;
  private final JLabel sourceLabel;
  private final JTextField sourceField;
  private final JLabel commentLabel;
  private final JTextArea commentField;
  private final JCheckBox imageNsfwCheck;
  private final JCheckBox sourceNsfwCheck;
  private final JLabel imageLabel;
  private final JButton selectImageButton;
  private final JButton findSimilarButton;
  private final JButton resetButton;
  private final JButton saveButton;

  public EditReservePanel(Window parent) {
    this.parent = parent;
    editId = null;
    selectedImageFile = null;
    lastSelectedDir = null;
    imageUpdated = false;
    modified = false;

    titleLabel = new JLabel("Title");
    titleField = new JTextField();
    artistLabel = new JLabel("Artist");
    artistField = new JTextField();
    sourceLabel = new JLabel("Source");
    sourceField = new JTextField();
    commentLabel = new JLabel("Comment");
    commentField = new JTextArea(3, 30);
    imageNsfwCheck = new JCheckBox("Is image NSFW?");
    sourceNsfwCheck = new JCheckBox("Is source NSFW?");
    imageLabel = new JLabel();
    imageLabel.setBorder(new StrokeBorder(new BasicStroke(1), Color.BLACK));
    selectImageButton = new JButton("Select image...");
    findSimilarButton = new JButton("Find similar");
    resetButton = new JButton("Reset");
    saveButton = new JButton("Add");

    final int FH = selectImageButton.getPreferredSize().height;

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup()
                .addComponent(titleLabel)
                .addComponent(titleField, 100, 100, 400)
                .addComponent(artistLabel)
                .addComponent(artistField, 100, 100, 400)
                .addComponent(sourceLabel)
                .addComponent(sourceField, 100, 200, 400)
                .addComponent(commentLabel)
                .addComponent(commentField)
                .addComponent(imageNsfwCheck)
                .addComponent(sourceNsfwCheck)
            )
            .addGroup(layout.createParallelGroup()
                .addComponent(imageLabel, GroupLayout.Alignment.CENTER)
                .addComponent(selectImageButton, GroupLayout.Alignment.CENTER)
                .addComponent(findSimilarButton, GroupLayout.Alignment.CENTER)
            )
        )
        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(resetButton)
            .addGap(GAP_SIZE)
            .addComponent(saveButton)
        )
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addComponent(titleLabel)
                .addComponent(titleField, FH, FH, FH)
                .addGap(GAP_SIZE)
                .addComponent(artistLabel)
                .addComponent(artistField, FH, FH, FH)
                .addGap(GAP_SIZE)
                .addComponent(sourceLabel)
                .addComponent(sourceField, FH, FH, FH)
                .addGap(GAP_SIZE)
                .addComponent(commentLabel)
                .addComponent(commentField, FH, FH, FH)
                .addGap(GAP_SIZE)
                .addComponent(imageNsfwCheck)
                .addGap(GAP_SIZE)
                .addComponent(sourceNsfwCheck)
            )
            .addGroup(layout.createSequentialGroup()
                .addComponent(imageLabel)
                .addComponent(selectImageButton, FH, FH, FH)
                .addGap(GAP_SIZE)
                .addComponent(findSimilarButton, FH, FH, FH)
            )
        )
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(resetButton)
            .addComponent(saveButton)
        )
    );
    setLayout(layout);

    DocumentChangedListener markModified = e -> modified = true;
    titleField.getDocument().addDocumentListener(markModified);
    artistField.getDocument().addDocumentListener(markModified);
    sourceField.getDocument().addDocumentListener(markModified);
    commentField.getDocument().addDocumentListener(markModified);
    imageNsfwCheck.addChangeListener(e -> modified = true);
    sourceNsfwCheck.addChangeListener(e -> modified = true);

    selectImageButton.addActionListener(event -> {
      if (lastSelectedDir == null) {
        lastSelectedDir = Paths.get(Objects.requireNonNullElse(DailyImagePoster.getInstance().preferences.getString(DailyImagePoster.PREF_LAST_IMAGE_DIRECTORY), "."));
      }
      JFileChooser chooser = new JFileChooser(lastSelectedDir.toFile());
      chooser.setFileFilter(new FileNameExtensionFilter("Images (*.png, *.jpeg, *.jpg)", "png", "jpeg", "jpg"));
      if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
        selectedImageFile = chooser.getSelectedFile().toPath();
        lastSelectedDir = selectedImageFile.getParent();
        try (InputStream in = Files.newInputStream(selectedImageFile)) {
          BufferedImage bufImg = ImageIO.read(in);
          UIHelper.updateImageLabel(imageLabel, bufImg, IMAGE_SIZE);
        } catch (IOException e) {
          DailyImagePoster.LOG.error("Could not load image {}", selectedImageFile, e);
        }
      }
    });
    findSimilarButton.addActionListener(event -> {
      if (selectedImageFile != null) {
        findSimilarButton.setEnabled(false);
        SwingUtilities.invokeLater(() -> {
          List<ImageManager.SimilarImage> images = DailyImagePoster.getInstance().images().findSimilarImages(selectedImageFile, DailyImagePoster.getInstance().preferences.getDouble(DailyImagePoster.PREF_SIMILARITY_THRESHOLD));
          if (images.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No similar images found.");
          } else {
            JDialog dialog = new JDialog(parent, "Similar images found");
            dialog.setContentPane(new SimilarImagesPanel(images, () -> {
              dialog.pack();
              dialog.setLocationRelativeTo(parent);
            }));
            dialog.setVisible(true);
          }
          findSimilarButton.setEnabled(true);
        });
      }
    });
    resetButton.addActionListener(event -> {
      boolean shouldReset;
      if (modified) {
        int answer = JOptionPane.showConfirmDialog(parent, "Are you sure you want to reset all fields?", "Question", JOptionPane.YES_NO_OPTION);
        shouldReset = answer == JOptionPane.YES_OPTION;
      } else {
        shouldReset = true;
      }
      if (shouldReset) {
        updateDetails(null);
        modified = false;
      }
    });
    saveButton.addActionListener(event -> {
      if (editId == null) {
        if (selectedImageFile == null) {
          JOptionPane.showMessageDialog(parent, "Must select an image to create a reserve.", "Warning", JOptionPane.WARNING_MESSAGE);
          return;
        }
        List<ImageManager.SimilarImage> images = DailyImagePoster.getInstance().images().findSimilarImages(selectedImageFile, DailyImagePoster.getInstance().preferences.getDouble(DailyImagePoster.PREF_SIMILARITY_THRESHOLD));
        if (!images.isEmpty()) {
          int answer = JOptionPane.showConfirmDialog(parent, "Found " + images.size() + " similar image(s). Would you like to reserve this image anyways?", "Question", JOptionPane.YES_NO_OPTION);
          if (answer == JOptionPane.NO_OPTION) {
            JDialog dialog = new JDialog(parent, "Similar images found");
            dialog.setContentPane(new SimilarImagesPanel(images, () -> {
              dialog.pack();
              dialog.setLocationRelativeTo(parent);
            }));
            dialog.setVisible(true);
            return;
          } else if (answer != JOptionPane.YES_OPTION) {
            return;
          }
        }
        String fileName;
        try {
          fileName = DailyImagePoster.getInstance().reserves().copy(selectedImageFile);
        } catch (IOException e) {
          DailyImagePoster.LOG.error("Could not copy image for reserve {}", selectedImageFile, e);
          JOptionPane.showMessageDialog(parent, "Could not copy image for reserve.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        Reserve reserve = new Reserve(
            UUID.randomUUID(),
            fileName,
            titleField.getText(),
            artistField.getText(),
            sourceField.getText(),
            commentField.getText(),
            imageNsfwCheck.isSelected(),
            sourceNsfwCheck.isSelected(),
            LocalDateTime.now()
        );
        DailyImagePoster.getInstance().reserves().getRepo().add(reserve);
        DailyImagePoster.LOG.info("Added new reserve image: id={}, fileName={}", reserve.id(), reserve.fileName());
        JOptionPane.showMessageDialog(parent, "Successfully added reserve image.");
        editId = reserve.id();
        saveButton.setText("Save changes");
        modified = false;
      } else {
        if (selectedImageFile == null) {
          JOptionPane.showMessageDialog(parent, "Cannot save a reserve with no image.", "Warning", JOptionPane.WARNING_MESSAGE);
          return;
        }
        Reserve orig = DailyImagePoster.getInstance().reserves().getRepo().get(editId);
        if (orig == null) {
          JOptionPane.showMessageDialog(parent, "Unknown reserve ID: " + editId + ". Cannot make any changes.", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        String fileName;
        if (imageUpdated) {
          DailyImagePoster.getInstance().reserves().delete(orig.fileName());
          try {
            fileName = DailyImagePoster.getInstance().reserves().copy(selectedImageFile);
          } catch (IOException e) {
            DailyImagePoster.LOG.error("Could not copy image for reserve {}", selectedImageFile, e);
            JOptionPane.showMessageDialog(parent, "Could not copy image for reserve.\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }
        } else {
          fileName = orig.fileName();
        }
        Reserve reserve = new Reserve(
            editId,
            fileName,
            titleField.getText(),
            artistField.getText(),
            sourceField.getText(),
            commentField.getText(),
            imageNsfwCheck.isSelected(),
            sourceNsfwCheck.isSelected(),
            null // whenCreated is not updated
        );
        DailyImagePoster.getInstance().reserves().getRepo().update(reserve);
        DailyImagePoster.LOG.info("Updated reserve image: id={}", reserve.id());
        JOptionPane.showMessageDialog(parent, "Successfully updated reserve image.");
        modified = false;
      }
    });

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
              Path path = droppedFiles.get(0).toPath();
              updateImage(path);
            }
          } catch (UnsupportedFlavorException | IOException e) {
            LOG.warn("Could not perform drag-and-drop operation", e);
          }
        }
      });
      // why is this a checked exception????
    } catch (TooManyListenersException e) {
      throw new RuntimeException(e);
    }

    updateDetails(null);
  }

  private void updateImage(@Nullable Path path) {
    try {
      BufferedImage img;
      if (path == null) {
        img = ImageIO.read(IOHelper.getResource("noimage.png"));
      } else {
        try (InputStream in = Files.newInputStream(path)) {
          img = ImageIO.read(in);
          if (img == null) {
            String[] baseAndExt = StringHelper.getFileNameBaseAndExtension(path.getFileName().toString());
            JOptionPane.showMessageDialog(parent, "Could not load image, possibly an invalid format: " + baseAndExt[1], "Warning", JOptionPane.WARNING_MESSAGE);
          } else {
            selectedImageFile = path;
            imageUpdated = true;
            modified = true;
          }
        }
      }
      if (img != null) {
        UIHelper.updateImageLabel(imageLabel, img, IMAGE_SIZE);
      }
    } catch (IOException e) {
      DailyImagePoster.LOG.error("Could not update image", e);
    }
  }

  public void updateDetails(@Nullable Reserve reserve) {
    if (reserve == null) {
      saveButton.setText("Add reserve");
      editId = null;
      titleField.setText("");
      artistField.setText("");
      sourceField.setText("");
      commentField.setText("");
      imageNsfwCheck.setSelected(false);
      sourceNsfwCheck.setSelected(false);
      updateImage(null);
    } else {
      saveButton.setText("Save changes");
      editId = reserve.id();
      titleField.setText(reserve.title());
      artistField.setText(reserve.artist());
      sourceField.setText(reserve.source());
      commentField.setText(reserve.comment());
      imageNsfwCheck.setSelected(reserve.imageNsfw());
      sourceNsfwCheck.setSelected(reserve.sourceNsfw());
      Path imgPath = DailyImagePoster.getInstance().reserves().getPath(reserve.fileName());
      if (Files.exists(imgPath)) {
        updateImage(imgPath);
      } else {
        updateImage(null);
        JOptionPane.showMessageDialog(parent, "Could not find image at " + reserve.fileName(), "Warning", JOptionPane.WARNING_MESSAGE);
      }
    }
    modified = false;
    parent.pack();
  }

}
