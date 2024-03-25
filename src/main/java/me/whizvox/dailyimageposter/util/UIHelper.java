package me.whizvox.dailyimageposter.util;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.gui.DIPMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

public class UIHelper {

  public static final int GAP_SIZE = 10;

  public static void updateImageLabel(JLabel label, BufferedImage image, int imageSize) {
    float ratio = (float) image.getWidth() / image.getHeight();
    int width, height;
    if (ratio > 1) {
      width = imageSize;
      height = (int) (imageSize / ratio);
    } else {
      width = (int) (imageSize * ratio);
      height = imageSize;
    }
    label.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
  }

  public static void browse(String uri) {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
      try {
        Desktop.getDesktop().browse(URI.create(uri));
      } catch (IOException ex) {
        DailyImagePoster.LOG.warn("Could not open link in browser", ex);
      }
    }
  }

  public static void addMenuBar(JFrame frame) {
    JMenuBar menuBar = new DIPMenuBar(frame);
    frame.setJMenuBar(menuBar);
  }

}
