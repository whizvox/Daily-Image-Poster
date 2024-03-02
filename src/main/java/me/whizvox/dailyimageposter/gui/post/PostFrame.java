package me.whizvox.dailyimageposter.gui.post;

import javax.swing.*;

public class PostFrame extends JFrame {

  private final PostPanel panel;

  public PostFrame() {
    setVisible(false);
    setContentPane(panel = new PostPanel(this));
  }

  public PostPanel getPanel() {
    return panel;
  }

}
