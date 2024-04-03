package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;

import javax.swing.*;
import java.util.Map;

public class GeneralPrefsPanel extends AbstractPrefsPanel {

  private final JCheckBox openAfterBox;

  public GeneralPrefsPanel(PreferencesPanel parent) {
    super(parent);

    openAfterBox = new JCheckBox("Open link after post?");

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(openAfterBox)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(openAfterBox)
    );
    setLayout(layout);

    parent.addChangeListener(openAfterBox);
  }

  @Override
  public void saveChanges(Map<String, Object> prefs) {
    prefs.put(DailyImagePoster.PREF_OPEN_AFTER_POST, openAfterBox.isSelected());
  }

}
