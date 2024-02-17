package me.whizvox.dailyimageposter.gui.search;

import me.whizvox.dailyimageposter.util.UIHelper;

import javax.swing.*;

public class SearchPostsFrame extends JFrame {

  private final SearchPostsPanel panel;

  public SearchPostsFrame() {
    setTitle("Search posts | Daily Image Poster");

    panel = new SearchPostsPanel(this);
    setContentPane(panel);
    UIHelper.addMenuBar(this);
    pack();
  }

}
