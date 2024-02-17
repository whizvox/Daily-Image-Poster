package me.whizvox.dailyimageposter.gui.post;

import me.whizvox.dailyimageposter.util.UIHelper;

import javax.swing.*;

public class PostFrame extends JFrame {

  private final PostPanel panel;

  public PostFrame() {
    setTitle("Daily Image Poster");

    setVisible(false);
    setContentPane(panel = new PostPanel(this));
    UIHelper.addMenuBar(this);
    pack();

    setResizable(false);
  }

  public PostPanel getPanel() {
    return panel;
  }

}
