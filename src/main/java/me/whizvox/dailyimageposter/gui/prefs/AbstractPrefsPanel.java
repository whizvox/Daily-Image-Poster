package me.whizvox.dailyimageposter.gui.prefs;

import javax.swing.*;
import java.util.Map;

public abstract class AbstractPrefsPanel extends JPanel {

  protected final PreferencesPanel parent;

  public AbstractPrefsPanel(PreferencesPanel parent) {
    this.parent = parent;
  }

  public abstract void saveChanges(Map<String, Object> prefs);

}
