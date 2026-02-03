package me.whizvox.dailyimageposter.gui.reserve;

import javax.swing.*;

public class EditReserveFrame extends JFrame {

  public final EditReservePanel panel;

  public EditReserveFrame() {
    panel = new EditReservePanel(this);
    setContentPane(panel);
  }

}
