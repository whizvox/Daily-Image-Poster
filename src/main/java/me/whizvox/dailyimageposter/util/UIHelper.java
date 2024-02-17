package me.whizvox.dailyimageposter.util;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.gui.post.PostFrame;
import me.whizvox.dailyimageposter.gui.prefs.PreferencesDialog;
import me.whizvox.dailyimageposter.gui.prefs.PreferencesPanel;
import me.whizvox.dailyimageposter.gui.search.SearchPostsFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    menuBar.add(fileMenu);
    JMenuItem fileAbout = new JMenuItem("About");
    JMenuItem fileConfig = new JMenuItem("Preferences");
    fileConfig.addActionListener(event -> new PreferencesDialog(frame, "Preferences", true));
    JMenuItem fileExit = new JMenuItem("Exit");
    fileMenu.add(fileAbout);
    fileMenu.add(fileConfig);
    fileMenu.addSeparator();
    fileMenu.add(fileExit);

    JMenu postsMenu = new JMenu("Post");
    postsMenu.setMnemonic('P');
    menuBar.add(postsMenu);
    JMenuItem postsCreate = new JMenuItem("Create new post");
    if (frame.getClass() == PostFrame.class) {
      postsCreate.setEnabled(false);
    } else {
      postsCreate.addActionListener(event -> DailyImagePoster.getInstance().changeFrame(PostFrame::new, null));
    }
    JMenuItem postsSearch = new JMenuItem("Search posts");
    if (frame.getClass() == SearchPostsFrame.class) {
      postsSearch.setEnabled(false);
    } else {
      postsSearch.addActionListener(event -> DailyImagePoster.getInstance().changeFrame(SearchPostsFrame::new, "Search"));
    }
    JMenuItem postsRebuild = new JMenuItem("Rebuild database");
    postsMenu.add(postsCreate);
    postsMenu.add(postsSearch);
    postsMenu.add(postsRebuild);

    JMenu backupsMenu = new JMenu("Backups");
    backupsMenu.setMnemonic('B');
    menuBar.add(backupsMenu);
    JMenuItem bkpView = new JMenuItem("View backups");
    JMenuItem bkpCreate = new JMenuItem("Create backup");
    backupsMenu.add(bkpView);
    backupsMenu.add(bkpCreate);
    frame.setJMenuBar(menuBar);
  }

}
