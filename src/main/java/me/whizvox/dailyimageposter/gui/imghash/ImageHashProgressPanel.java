package me.whizvox.dailyimageposter.gui.imghash;

import me.whizvox.dailyimageposter.DailyImagePoster;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class ImageHashProgressPanel extends JPanel implements PropertyChangeListener {

  private final DailyImagePoster app;
  private final JLabel statusLabel;
  private final JProgressBar progressBar;
  private final JButton cancelButton;
  private final JButton okButton;
  private final Task task;

  public ImageHashProgressPanel(Window owner, boolean forceUpdate) {
    app = DailyImagePoster.getInstance();
    statusLabel = new JLabel("Hashing images...");
    progressBar = new JProgressBar();
    progressBar.setValue(0);
    progressBar.setStringPainted(true);
    cancelButton = new JButton("Cancel");
    okButton = new JButton("OK");
    okButton.setEnabled(false);

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(statusLabel)
        .addComponent(progressBar)
        .addGroup(GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
            .addComponent(cancelButton)
            .addComponent(okButton)
        )
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(statusLabel)
        .addGap(GAP_SIZE)
        .addComponent(progressBar)
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(cancelButton)
            .addComponent(okButton)
        )
    );
    setLayout(layout);

    List<Path> paths = app.images().applyStream(Stream::toList);
    task = new Task(paths, forceUpdate);
    task.addPropertyChangeListener(this);

    cancelButton.addActionListener(e -> {
      cancel();
      owner.dispose();
    });
    okButton.addActionListener(e -> owner.dispose());
  }

  public void beginHashing() {
    task.execute();
  }

  public void cancel() {
    task.cancel();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getPropertyName().equals("progress")) {
      progressBar.setValue((int) event.getNewValue());
    }
  }

  private class Task extends SwingWorker<Void, Void> {

    private final List<Path> images;
    private final boolean forceUpdate;

    private int hashedImages;
    private boolean shouldCancel;

    public Task(List<Path> images, boolean forceUpdate) {
      this.images = images;
      this.forceUpdate = forceUpdate;
      shouldCancel = false;
    }

    public void cancel() {
      shouldCancel = true;
    }

    @Override
    protected Void doInBackground() throws Exception {
      setProgress(0);
      int count = 0;
      hashedImages = 0;
      for (Path imagePath : images) {
        if (shouldCancel) {
          break;
        }
        if (app.images().addImageHash(imagePath.getFileName().toString(), forceUpdate)) {
          hashedImages++;
        }
        count++;
        int progress = (int) (((float) count / images.size()) * 100);
        setProgress(progress);
      }
      return null;
    }

    @Override
    protected void done() {
      statusLabel.setText("Hashed " + hashedImages + " images");
      okButton.setEnabled(true);
      cancelButton.setEnabled(false);
      Toolkit.getDefaultToolkit().beep();
    }

  }

}
