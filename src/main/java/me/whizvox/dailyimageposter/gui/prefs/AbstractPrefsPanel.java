package me.whizvox.dailyimageposter.gui.prefs;

import javax.swing.*;
import java.util.Properties;

public abstract class AbstractPrefsPanel extends JPanel {

  protected final PreferencesPanel parent;

  public AbstractPrefsPanel(PreferencesPanel parent) {
    this.parent = parent;
  }

  public abstract void saveChanges(Properties props);

}
