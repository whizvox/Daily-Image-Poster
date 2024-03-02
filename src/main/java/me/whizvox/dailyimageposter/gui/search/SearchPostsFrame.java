package me.whizvox.dailyimageposter.gui.search;

import javax.swing.*;

public class SearchPostsFrame extends JFrame {

  private final SearchPostsPanel panel;

  public SearchPostsFrame() {
    panel = new SearchPostsPanel(this);
    setContentPane(panel);
  }

}
