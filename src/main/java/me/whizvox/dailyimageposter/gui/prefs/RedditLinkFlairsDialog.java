package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.reddit.pojo.LinkFlair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;

public class RedditLinkFlairsDialog extends JDialog {

  private final DailyImagePoster app;
  private final Panel panel;
  private LinkFlair selectedFlair;

  public RedditLinkFlairsDialog(Frame owner) {
    super(owner, "Select flair", true);
    this.app = DailyImagePoster.getInstance();
    panel = new Panel();
    selectedFlair = null;
    setContentPane(panel);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        // user is trying to close the window
        selectedFlair = null;
        dispose();
      }
    });
    pack();
    setLocationRelativeTo(owner);
  }

  public class Panel extends JPanel {

    private final LinkFlairsTableModel tableModel;
    private final JTable table;

    public Panel() {
      tableModel = new LinkFlairsTableModel();
      table = new JTable(tableModel);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane scrollPane = new JScrollPane(table);
      JButton useButton = new JButton("Use flair");
      selectedFlair = null;

      GroupLayout layout = new GroupLayout(this);
      layout.setAutoCreateGaps(true);
      layout.setAutoCreateContainerGaps(true);
      layout.setHorizontalGroup(layout.createParallelGroup()
          .addComponent(scrollPane)
          .addComponent(useButton, GroupLayout.Alignment.TRAILING)
      );
      layout.setVerticalGroup(layout.createSequentialGroup()
          .addComponent(scrollPane, 100, 200, 400)
          .addComponent(useButton)
      );
      setLayout(layout);

      table.getSelectionModel().addListSelectionListener(e -> {
        selectedFlair = tableModel.getFlair(((ListSelectionModel) e.getSource()).getLeadSelectionIndex());
        LOG.debug("Selected flair: {}", selectedFlair);
      });
      useButton.addActionListener(e -> dispose());
    }

    public void refreshFlairs(String subreddit) {
      app.getRedditClient().getLinkFlairs(subreddit).whenComplete((flairs, e) -> {
        if (e != null) {
          LOG.warn("Could not retrieve link flairs", e);
        } else {
          tableModel.setData(flairs);
        }
      });
    }
  }

  public static LinkFlair selectFlair(Frame owner, String subreddit) {
    var dialog = new RedditLinkFlairsDialog(owner);
    dialog.panel.refreshFlairs(subreddit);
    dialog.setVisible(true);
    dialog.dispose();
    return dialog.selectedFlair;
  }

}
