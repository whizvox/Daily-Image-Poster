package me.whizvox.dailyimageposter.gui;

import me.whizvox.dailyimageposter.db.Post;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostTableModel extends AbstractTableModel {

  private static final String[] COLUMN_NAMES = {"ID", "Number", "Title", "Artist", "Posted"};

  private final List<Post> posts;

  public PostTableModel() {
    this.posts = new ArrayList<>();
  }

  public void setData(Iterable<Post> newData) {
    posts.clear();
    newData.forEach(posts::add);
    fireTableDataChanged();
  }

  @Nullable
  public Post getPost(int rowIndex) {
    if (rowIndex >= 0 && rowIndex < posts.size()) {
      return posts.get(rowIndex);
    }
    return null;
  }

  @Override
  public int getRowCount() {
    return posts.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Post post = posts.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> String.valueOf(post.id());
      case 1 -> post.formatNumber();
      case 2 -> post.title();
      case 3 -> post.artist();
      case 4 -> Optional.ofNullable(post.whenPosted()).map(DateTimeFormatter.ISO_DATE_TIME::format).orElse(null);
      default -> null;
    };
  }

  public interface SelectedListener {
    void onSelected(@Nullable Post post);
  }

}
