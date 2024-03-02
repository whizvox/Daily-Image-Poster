package me.whizvox.dailyimageposter.gui.debug;

import javax.swing.*;

public class RedditDebugFrame extends JFrame {

  public RedditDebugFrame() {

    setContentPane(new RedditDebugPanel(this));

  }

}
