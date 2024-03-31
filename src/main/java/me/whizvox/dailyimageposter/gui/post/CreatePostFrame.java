package me.whizvox.dailyimageposter.gui.post;

import javax.swing.*;

public class CreatePostFrame extends JFrame {

  private final CreatePostPanel panel;

  public CreatePostFrame() {
    setVisible(false);
    setContentPane(panel = new CreatePostPanel(this));
  }

  public CreatePostPanel getPanel() {
    return panel;
  }

}
