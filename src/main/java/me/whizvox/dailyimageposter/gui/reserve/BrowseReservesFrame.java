package me.whizvox.dailyimageposter.gui.reserve;

import javax.swing.*;
import java.awt.*;

public class BrowseReservesFrame extends JFrame {

  public final BrowseReservesPanel panel;

  public BrowseReservesFrame() throws HeadlessException {
    panel = new BrowseReservesPanel(this);
    setContentPane(panel);
  }

}
