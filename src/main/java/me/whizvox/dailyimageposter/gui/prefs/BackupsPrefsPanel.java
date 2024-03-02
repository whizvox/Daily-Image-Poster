package me.whizvox.dailyimageposter.gui.prefs;

import javax.swing.*;
import java.util.Map;

public class BackupsPrefsPanel extends AbstractPrefsPanel {

  private final JTextField maxCountField;

  public BackupsPrefsPanel(PreferencesPanel parent) {
    super(parent);

    JLabel maxCountLabel = new JLabel("Max Backup Count");
    maxCountField = new JTextField("30");

    int fh = maxCountField.getPreferredSize().height;

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(maxCountLabel)
        .addComponent(maxCountField)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(maxCountLabel)
        .addComponent(maxCountField, fh, fh, fh)
    );
    setLayout(layout);

    parent.addChangeListeners(maxCountField);
  }

  @Override
  public void saveChanges(Map<String, Object> prefs) {

  }


}
