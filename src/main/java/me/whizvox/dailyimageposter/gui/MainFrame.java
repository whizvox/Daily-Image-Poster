package me.whizvox.dailyimageposter.gui;

import javax.swing.*;

public class MainFrame extends JFrame {

  private final PostPanel panel;

  public MainFrame() {
    setTitle("Daily Image Poster");

    setVisible(false);
    setContentPane(panel = new PostPanel());
    pack();
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic('F');
    menuBar.add(fileMenu);
    JMenuItem fileAbout = new JMenuItem("About");
    JMenuItem fileConfig = new JMenuItem("Preferences");
    JMenuItem fileExit = new JMenuItem("Exit");
    fileMenu.add(fileAbout);
    fileMenu.add(fileConfig);
    fileMenu.addSeparator();
    fileMenu.add(fileExit);

    JMenu postsMenu = new JMenu("Posts");
    postsMenu.setMnemonic('P');
    menuBar.add(postsMenu);
    JMenuItem postsSearch = new JMenuItem("Search posts");
    postsSearch.addActionListener(event -> searchPosts());
    JMenuItem postsRebuild = new JMenuItem("Rebuild database");
    postsMenu.add(postsSearch);
    postsMenu.add(postsRebuild);

    JMenu backupsMenu = new JMenu("Backups");
    backupsMenu.setMnemonic('B');
    menuBar.add(backupsMenu);
    JMenuItem bkpView = new JMenuItem("View backups");
    JMenuItem bkpCreate = new JMenuItem("Create backup");
    backupsMenu.add(bkpView);
    backupsMenu.add(bkpCreate);

    setJMenuBar(menuBar);

    setResizable(false);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setVisible(true);
  }

  public PostPanel getPanel() {
    return panel;
  }

  private void searchPosts() {
    JDialog dialog = new JDialog(this, "Search posts", true);
    dialog.setContentPane(new SearchPostsPanel(dialog::dispose));
    dialog.pack();
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

}
