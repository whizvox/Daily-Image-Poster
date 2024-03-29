package me.whizvox.dailyimageposter.gui.imghash;

import javax.swing.*;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class ImageHashPanel extends JPanel {

  public ImageHashPanel(JFrame parent) {
    JCheckBox forceCheck = new JCheckBox("Force update");
    JButton updateButton = new JButton("Update image hashes");

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(forceCheck)
        .addComponent(updateButton)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(forceCheck)
        .addGap(GAP_SIZE)
        .addComponent(updateButton)
    );
    setLayout(layout);

    updateButton.addActionListener(e -> new ImageHashProgressDialog(parent, true, forceCheck.isSelected()));
  }

}
