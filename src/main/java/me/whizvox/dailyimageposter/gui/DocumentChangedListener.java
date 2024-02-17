package me.whizvox.dailyimageposter.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface DocumentChangedListener extends DocumentListener {

  void onChange(DocumentEvent event);

  @Override
  default void changedUpdate(DocumentEvent e) {
    onChange(e);
  }

  @Override
  default void insertUpdate(DocumentEvent e) {
    onChange(e);
  }

  @Override
  default void removeUpdate(DocumentEvent e) {
    onChange(e);
  }
}
