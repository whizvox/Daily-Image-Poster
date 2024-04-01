package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.reddit.pojo.LinkFlair;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class LinkFlairsTableModel extends AbstractTableModel {

  private static final String[] COLUMN_NAMES = {"Flair", "Is Editable?"};

  private final List<LinkFlair> flairs;

  public LinkFlairsTableModel() {
    this.flairs = new ArrayList<>();
  }

  public void setData(Iterable<LinkFlair> newData) {
    flairs.clear();
    newData.forEach(flairs::add);
    fireTableDataChanged();
  }

  @Nullable
  public LinkFlair getFlair(int rowIndex) {
    if (rowIndex >= 0 && rowIndex < flairs.size()) {
      return flairs.get(rowIndex);
    }
    return null;
  }

  @Override
  public int getRowCount() {
    return flairs.size();
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
    LinkFlair flair = flairs.get(rowIndex);
    return switch (columnIndex) {
      case 0 -> flair.text;
      case 1 -> flair.textEditable ? "Yes" : "No";
      default -> null;
    };
  }

}
