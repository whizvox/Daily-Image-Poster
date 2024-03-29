package me.whizvox.dailyimageposter.gui.imghash;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ImageHashProgressDialog extends JDialog {

  public ImageHashProgressDialog(Frame owner, boolean modal, boolean forceUpdate) {
    super(owner, "Image hashes", modal);
    var panel = new ImageHashProgressPanel(this, forceUpdate);
    setContentPane(panel);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        panel.cancel();
        ImageHashProgressDialog.this.dispose();
      }
    });
    pack();
    setLocationRelativeTo(owner);
    panel.beginHashing();
    setVisible(true);
  }

}
