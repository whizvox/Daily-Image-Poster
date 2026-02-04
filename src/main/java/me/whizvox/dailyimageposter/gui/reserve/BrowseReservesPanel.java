package me.whizvox.dailyimageposter.gui.reserve;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.gui.post.CreatePostFrame;
import me.whizvox.dailyimageposter.reserve.Reserve;
import me.whizvox.dailyimageposter.util.UIHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.StrokeBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class BrowseReservesPanel extends JPanel {

  private final Window parent;

  private final JPanel entriesPanel;
  private final GridLayout entriesLayout;
  private final JButton deleteButton;
  private final JButton editButton;
  private final JButton postButton;
  private final JButton addButton;

  private Reserve selectedReserve;

  public BrowseReservesPanel(Window parent) {
    this.parent = parent;
    selectedReserve = null;

    entriesPanel = new JPanel();
    entriesLayout = new GridLayout(0, 4);
    entriesPanel.setLayout(entriesLayout);
    JScrollPane scrollPane = new JScrollPane(entriesPanel);

    deleteButton = new JButton("Delete");
    editButton = new JButton("Edit");
    postButton = new JButton("Post");
    addButton = new JButton("Add new");

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(scrollPane, 526, 526, 526)
        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(deleteButton)
            .addComponent(editButton)
            .addComponent(postButton)
            .addGap(GAP_SIZE * 2)
            .addComponent(addButton)
        )
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(scrollPane, 100, 200, 400)
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(deleteButton)
            .addComponent(editButton)
            .addComponent(postButton)
            .addComponent(addButton)
        )
    );
    setLayout(layout);

    deleteButton.setEnabled(false);
    deleteButton.addActionListener(event -> {
      if (selectedReserve != null) {
        int answer = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete this reserve?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
          DailyImagePoster.getInstance().reserves().delete(selectedReserve.fileName());
          JOptionPane.showMessageDialog(parent, "Successfully deleted reserve image.");
          selectedReserve = null;
          deleteButton.setEnabled(false);
          editButton.setEnabled(false);
          postButton.setEnabled(false);
          updateReserves();
        }
      }
    });
    editButton.setEnabled(false);
    editButton.addActionListener(event -> {
      if (selectedReserve != null) {
        DailyImagePoster.getInstance().changeFrame(() -> {
          EditReserveFrame editFrame = new EditReserveFrame();
          editFrame.panel.updateDetails(selectedReserve);
          return editFrame;
        }, "Edit reserve image");
      }
    });
    postButton.setEnabled(false);
    postButton.addActionListener(event -> {
      if (selectedReserve != null) {
        DailyImagePoster.getInstance().changeFrame(() -> {
          CreatePostFrame frame = new CreatePostFrame();
          frame.getPanel().prepareReserve(selectedReserve);
          return frame;
        }, "Post image");
      }
    });
    addButton.addActionListener(event -> {
      DailyImagePoster.getInstance().changeFrame(EditReserveFrame::new, "Add reserve image");
    });

    updateReserves();
  }

  public void updateReserves() {
    entriesPanel.removeAll();
    List<Reserve> reserves = DailyImagePoster.getInstance().reserves().getRepo().getAll();
    if (reserves.isEmpty()) {
      entriesPanel.add(new JLabel("No reserves found..."));
      entriesPanel.updateUI();
    } else {
      entriesPanel.add(new JLabel("Loading..."));
      entriesPanel.updateUI();
      SwingUtilities.invokeLater(() -> {
        List<JLabel> imageLabels = new ArrayList<>();
        reserves.forEach(reserve -> {
          Path path = DailyImagePoster.getInstance().reserves().getPath(reserve.fileName());
          if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
              BufferedImage img = ImageIO.read(in);
              JLabel label = new JLabel();
              label.setBorder(new StrokeBorder(new BasicStroke(1), Color.BLACK));
              UIHelper.updateImageLabel(label, img, 128);
              label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                  selectedReserve = reserve;
                  for (Component component : entriesPanel.getComponents()) {
                    ((JComponent) component).setBorder(new StrokeBorder(new BasicStroke(1), Color.BLACK));
                  }
                  label.setBorder(new StrokeBorder(new BasicStroke(1), Color.GREEN));
                  deleteButton.setEnabled(true);
                  editButton.setEnabled(true);
                  postButton.setEnabled(true);
                }
                @Override
                public void mouseEntered(MouseEvent event) {
                  if (selectedReserve != reserve) {
                    label.setBorder(new StrokeBorder(new BasicStroke(1), Color.MAGENTA));
                  }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                  if (selectedReserve != reserve) {
                    label.setBorder(new StrokeBorder(new BasicStroke(1), Color.BLACK));
                  }
                }
              });
              imageLabels.add(label);
            } catch (IOException e) {
              DailyImagePoster.LOG.warn("Could not load reserve image: id={}, path={}", reserve.id(), path, e);
            }
          } else {
            DailyImagePoster.LOG.warn("Could not find reserve image: id={}, fileName={}", reserve.id(), reserve.fileName());
          }
        });
        entriesPanel.removeAll();
        if (imageLabels.isEmpty()) {
          entriesPanel.add(new JLabel("No reserves found..."));
        } else {
          imageLabels.forEach(label -> entriesPanel.add(label));
        }
        entriesPanel.updateUI();
      });
    }
  }

}
