package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;

import javax.swing.*;

import java.util.Properties;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class PreferencesPanel extends JPanel {

  private final JScrollPane prefsScrollPane;
  private AbstractPrefsPanel currentPrefsPanel;
  private final JButton saveButton;

  private final Properties prefsCopy;
  private boolean unsavedChanges;

  public PreferencesPanel(PreferencesDialog parent) {
    JList<String> list = new JList<>(new String[] {"General", "Backups", "Reddit Credentials"});
    list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.setSelectedIndex(0);
    currentPrefsPanel = new GeneralPrefsPanel(this);
    prefsScrollPane = new JScrollPane(currentPrefsPanel);
    saveButton = new JButton("Save");
    saveButton.setEnabled(false);
    JButton confirmButton = new JButton("Confirm");
    prefsCopy = new Properties();
    prefsCopy.putAll(DailyImagePoster.getInstance().preferences);
    unsavedChanges = false;

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addGroup(layout.createSequentialGroup()
            .addComponent(list)
            .addGap(GAP_SIZE)
            .addComponent(prefsScrollPane, 200, 400, 800)
        )
        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(saveButton)
            .addComponent(confirmButton)
        )
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addComponent(list)
            .addComponent(prefsScrollPane, 200, 400, 800)
        )
        .addGroup(layout.createParallelGroup()
            .addComponent(saveButton)
            .addComponent(confirmButton)
        )
    );
    setLayout(layout);

    list.addListSelectionListener(e -> {
      int index = ((JList<String>) e.getSource()).getLeadSelectionIndex();
      switch (index) {
        case 0 -> changePrefsPanel(new GeneralPrefsPanel(this));
        case 1 -> changePrefsPanel(new BackupsPrefsPanel(this));
        case 2 -> changePrefsPanel(new CredentialsPrefsPanel(this));
        default -> DailyImagePoster.LOG.warn("Invalid preferences selection: {}", index);
      }
    });
    saveButton.addActionListener(event -> saveAll());
    confirmButton.addActionListener(event -> {
      saveAll();
      parent.dispose();
    });

  }

  public void saveAll() {
    currentPrefsPanel.saveChanges(prefsCopy);
    DailyImagePoster app = DailyImagePoster.getInstance();
    app.preferences.putAll(prefsCopy);
    app.savePreferences();
    unsavedChanges = false;
    saveButton.setEnabled(false);
  }

  private void changePrefsPanel(AbstractPrefsPanel panel) {
    currentPrefsPanel.saveChanges(prefsCopy);
    currentPrefsPanel = panel;
    prefsScrollPane.setViewportView(currentPrefsPanel);
  }

  public boolean hasUnsavedChanges() {
    return unsavedChanges;
  }

  public void markUnsavedChanges() {
    unsavedChanges = true;
    saveButton.setEnabled(true);
  }

}
