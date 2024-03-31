package me.whizvox.dailyimageposter.gui;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.gui.debug.RedditDebugFrame;
import me.whizvox.dailyimageposter.gui.imghash.ImageHashFrame;
import me.whizvox.dailyimageposter.gui.legacy.ImportLegacyFrame;
import me.whizvox.dailyimageposter.gui.post.CreatePostFrame;
import me.whizvox.dailyimageposter.gui.prefs.PreferencesDialog;
import me.whizvox.dailyimageposter.gui.search.SearchPostsFrame;

import javax.swing.*;

public class DIPMenuBar extends JMenuBar {

  public DIPMenuBar(JFrame parent) {
    DailyImagePoster app = DailyImagePoster.getInstance();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    add(fileMenu);
    JMenuItem fileAbout = new JMenuItem("About");
    JMenuItem fileConfig = new JMenuItem("Preferences");
    fileConfig.addActionListener(event -> new PreferencesDialog(parent, "Preferences", true));
    JMenuItem fileExit = new JMenuItem("Exit");
    fileMenu.add(fileAbout);
    fileMenu.add(fileConfig);
    fileMenu.addSeparator();
    fileMenu.add(fileExit);

    JMenu postsMenu = new JMenu("Posts");
    postsMenu.setMnemonic('P');
    add(postsMenu);
    JMenuItem postsCreate = new JMenuItem("Create new post");
    if (parent instanceof CreatePostFrame) {
      postsCreate.setEnabled(false);
    } else {
      postsCreate.addActionListener(event -> app.changeFrame(CreatePostFrame::new, null));
    }
    JMenuItem postsSearch = new JMenuItem("Search posts");
    if (parent instanceof SearchPostsFrame) {
      postsSearch.setEnabled(false);
    } else {
      postsSearch.addActionListener(event -> app.changeFrame(SearchPostsFrame::new, "Search"));
    }
    JMenuItem postsLegacy = new JMenuItem("Import legacy");
    if (parent instanceof ImportLegacyFrame) {
      postsLegacy.setEnabled(false);
    } else {
      postsLegacy.addActionListener(event -> app.changeFrame(ImportLegacyFrame::new, "Import legacy database"));
    }
    JMenuItem postsHashes = new JMenuItem("Image hashes");
    if (parent instanceof ImageHashFrame) {
      postsHashes.setEnabled(false);
    } else {
      postsHashes.addActionListener(e -> app.changeFrame(ImageHashFrame::new, "Image hashes"));
    }
    JMenuItem postsRebuild = new JMenuItem("Rebuild database");
    postsMenu.add(postsCreate);
    postsMenu.add(postsSearch);
    postsMenu.add(postsLegacy);
    postsMenu.add(postsHashes);
    postsMenu.add(postsRebuild);

    JMenu backupsMenu = new JMenu("Backups");
    backupsMenu.setMnemonic('B');
    add(backupsMenu);
    JMenuItem bkpView = new JMenuItem("View backups");
    JMenuItem bkpCreate = new JMenuItem("Create backup");
    backupsMenu.add(bkpView);
    backupsMenu.add(bkpCreate);

    JMenu debugMenu = new JMenu("Debug");
    add(debugMenu);
    JMenuItem debugReddit = new JMenuItem("Reddit Client");
    if (parent instanceof RedditDebugFrame) {
      debugReddit.setEnabled(false);
    } else {
      debugReddit.addActionListener(event -> app.changeFrame(RedditDebugFrame::new, "Reddit Client Debugging"));
    }
    debugMenu.add(debugReddit);
  }

}
