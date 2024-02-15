package me.whizvox.dailyimageposter.gui;

import me.whizvox.dailyimageposter.db.Post;

import javax.swing.*;
import java.util.List;

public class ListPostsPanel extends JPanel {

  public ListPostsPanel(List<Post> posts) {

    JLabel foundLabel = new JLabel("Found " + posts.size() + " post(s)");
    StringBuilder sb = new StringBuilder();
    posts.forEach(post -> {
      sb.append('[').append(post.number());
      if (post.subNumber() != 0) {
        sb.append('.').append(post.subNumber());
      }
      sb.append("] ").append(post.title()).append('\n');
    });
    JLabel listLabel = new JLabel(sb.toString());

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(foundLabel)
        .addComponent(listLabel)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(foundLabel)
        .addComponent(listLabel)
    );
    setLayout(layout);

  }

}
