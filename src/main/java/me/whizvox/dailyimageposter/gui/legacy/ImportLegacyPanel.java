package me.whizvox.dailyimageposter.gui.legacy;

import com.fasterxml.jackson.core.type.TypeReference;
import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.post.Post;
import me.whizvox.dailyimageposter.util.JsonHelper;
import me.whizvox.dailyimageposter.util.StringHelper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;
import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class ImportLegacyPanel extends JPanel {

  private final DailyImagePoster app;
  @Nullable
  private Path historyFile;

  private final JTextField dirField;
  private final JLabel statusLabel;
  private final JButton importButton;

  public ImportLegacyPanel() {
    app = DailyImagePoster.getInstance();
    historyFile = Optional.ofNullable(app.preferences.getString(DailyImagePoster.PREF_LAST_SELECTED_HISTORY))
        .map(s -> Paths.get(s).toAbsolutePath().normalize())
        .orElse(null);

    JLabel dirLabel = new JLabel("Legacy History Database");
    dirField = new JTextField(historyFile == null ? "" : historyFile.toString());
    JButton dirButton = new JButton("Browse");
    JCheckBox existingCheck = new JCheckBox("Ignore existing entries");
    existingCheck.setSelected(true);
    statusLabel = new JLabel(" ");
    importButton = new JButton("Import");
    if (historyFile == null) {
      importButton.setEnabled(false);
    }

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(dirLabel)
        .addGroup(layout.createSequentialGroup()
            .addComponent(dirField, 200, 400, 600)
            .addComponent(dirButton)
        )
        .addComponent(existingCheck)
        .addComponent(statusLabel, GroupLayout.Alignment.CENTER)
        .addComponent(importButton)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(dirLabel)
        .addGroup(layout.createParallelGroup()
            .addComponent(dirField)
            .addComponent(dirButton)
        )
        .addGap(GAP_SIZE)
        .addComponent(existingCheck)
        .addGap(GAP_SIZE)
        .addComponent(statusLabel)
        .addComponent(importButton)
    );
    setLayout(layout);

    dirButton.addActionListener(event -> chooseDirectory());
    importButton.addActionListener(event -> importLegacy(existingCheck.isSelected()));
  }

  private void chooseDirectory() {
    JFileChooser fileChooser = new JFileChooser(historyFile == null ? null : historyFile.getParent().toFile());
    fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
    int ret = fileChooser.showOpenDialog(this);
    if (ret == JFileChooser.APPROVE_OPTION) {
      historyFile = fileChooser.getSelectedFile().toPath().toAbsolutePath().normalize();
      dirField.setText(historyFile.toString());
      importButton.setEnabled(true);
      app.preferences.setString(DailyImagePoster.PREF_LAST_SELECTED_HISTORY, historyFile.toString());
    }
  }

  private void importLegacy(boolean ignoreExistingEntries) {
    statusLabel.setText("Importing...");
    importButton.setEnabled(false);
    SwingUtilities.invokeLater(() -> {
      try {
        Path imagesDir = historyFile.getParent().resolve("images");
        List<LegacyEntry> entries = JsonHelper.OBJECT_MAPPER.readValue(historyFile.toFile(), new TypeReference<>() {});
        if (!ignoreExistingEntries) {
          LOG.warn("Deleting all posts and images...");
          app.posts().deleteAll();
          app.images().consumeStream(stream -> stream.forEach(path -> {
            try {
              Files.delete(path);
            } catch (IOException e) {
              LOG.warn("Could not delete image " + path, e);
            }
          }));
        }
        AtomicInteger count = new AtomicInteger(0);
        entries.forEach(entry -> {
          Path imageFile = historyFile.getParent().resolve(entry.fileName);
          String fileName = imageFile.getFileName().toString();
          if (ignoreExistingEntries) {
            Post existingPost = DailyImagePoster.getInstance().posts().getByFileName(fileName);
            if (existingPost != null) {
              LOG.trace("Skipping already existing entry {}", fileName);
              return;
            }
          }
          Post post = new Post(UUID.randomUUID(), fileName, entry.id, (byte) 0, entry.title, entry.artist, entry.source, null, false, false, entry.redditPostId, null, entry.imgurId, null);
          app.posts().add(post);
          LOG.debug("Added post to database: {} ({})", post.formatNumber(), StringHelper.withCutoff(post.fileName(), 40));
          try {
            count.addAndGet(1);
            app.images().copy(imagesDir.resolve(fileName), post.number());
          } catch (IOException e) {
            LOG.warn("Could not import image " + fileName, e);
          }
        });
        statusLabel.setText("Imported " + count.get() + " post(s)");
        importButton.setEnabled(true);
      } catch (IOException e) {
        JOptionPane.showMessageDialog(ImportLegacyPanel.this, "Could not import legacy database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        LOG.warn("Could not import legacy database from " + historyFile, e);
      }
    });
  }

  public static class LegacyEntry {
    public int id;
    public String fileName;
    public String title;
    public String artist;
    public String source;
    public String imgurId;
    public String redditPostId;
  }

}
