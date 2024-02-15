package me.whizvox.dailyimageposter.gui;

import javax.swing.*;

public class MainFrame extends JFrame {

  public MainFrame() {
    setTitle("Daily Image Poster");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
  }

  public void update(JPanel panel, String title) {
    setVisible(false);
    setContentPane(panel);
    pack();
    if (title != null) {
      setTitle("Daily Image Poster | " + title);
    } else {
      setTitle("Daily Image Poster");
    }
    setResizable(false);
    setLocationRelativeTo(null);
    setVisible(true);
  }

}
