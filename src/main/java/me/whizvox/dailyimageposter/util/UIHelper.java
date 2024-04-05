package me.whizvox.dailyimageposter.util;

import me.whizvox.dailyimageposter.gui.DIPMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;

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
        LOG.warn("Could not open link in browser", ex);
      }
    }
  }

  public static void addMenuBar(JFrame frame) {
    JMenuBar menuBar = new DIPMenuBar(frame);
    frame.setJMenuBar(menuBar);
  }

  public static void setPlainFont(Component comp) {
    comp.setFont(comp.getFont().deriveFont(Font.PLAIN));
  }

  public static void addHyperlink(Component comp, String url) {
    Map<TextAttribute, Object> attributes = new HashMap<>();
    attributes.put(TextAttribute.FONT, comp.getFont());
    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    Font underlinedFont = Font.getFont(attributes);
    comp.setFont(underlinedFont);
    comp.setForeground(Color.BLUE);
    comp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    comp.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        browse(url);
      }
    });
  }

}
