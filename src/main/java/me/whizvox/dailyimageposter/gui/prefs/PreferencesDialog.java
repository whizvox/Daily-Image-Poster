package me.whizvox.dailyimageposter.gui.prefs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PreferencesDialog extends JDialog {

  public PreferencesDialog(Frame owner, String title, boolean modal) {
    super(owner, title, modal);
    PreferencesPanel prefsPanel = new PreferencesPanel(this);
    setContentPane(prefsPanel);
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        if (prefsPanel.hasUnsavedChanges()) {
          int selection = JOptionPane.showConfirmDialog(PreferencesDialog.this, "You have unsaved changes! Do you want to save them first?", "Warning!", JOptionPane.YES_NO_CANCEL_OPTION);
          if (selection == JOptionPane.YES_OPTION) {
            prefsPanel.saveAll();
          } else if (selection != JOptionPane.NO_OPTION) {
            return;
          }
        }
        PreferencesDialog.this.dispose();
      }
    });
    pack();
    setLocationRelativeTo(owner);
    setVisible(true);
  }

}
