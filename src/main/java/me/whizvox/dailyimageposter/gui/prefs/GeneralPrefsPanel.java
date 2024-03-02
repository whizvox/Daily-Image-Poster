package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.util.UIHelper;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class GeneralPrefsPanel extends AbstractPrefsPanel {

  private final JTextField imageQualityField;
  private final JTextField imageMinDimensionField;
  private final JTextField imageMaxSizeField;

  public GeneralPrefsPanel(PreferencesPanel parent) {
    super(parent);
    DailyImagePoster app = DailyImagePoster.getInstance();

    JLabel imgCompLabel = new JLabel("Image Compression Quality");
    Font plainFont = imgCompLabel.getFont().deriveFont(Font.PLAIN);
    JLabel imgCompInfo = new JLabel("(Integer between 0 and 100)");
    imgCompInfo.setFont(plainFont);
    imageQualityField = new JTextField(String.valueOf(app.preferences.getInt(DailyImagePoster.PREF_IMAGE_QUALITY)));
    JLabel imgMinDimLabel = new JLabel("Minimum Image Dimension");
    JLabel imgMinDimInfo = new JLabel("(Positive Integer)");
    imgMinDimInfo.setFont(plainFont);
    imageMinDimensionField = new JTextField(String.valueOf(app.preferences.getInt(DailyImagePoster.PREF_MIN_IMAGE_DIMENSION)));
    JLabel imgMaxSizeLabel = new JLabel("Maximum Image Size (bytes)");
    JLabel imgMaxSizeInfo = new JLabel("(Positive Integer)");
    imgMaxSizeInfo.setFont(plainFont);
    imageMaxSizeField = new JTextField(String.valueOf(app.preferences.getInt(DailyImagePoster.PREF_MAX_IMAGE_SIZE)));

    int fh = imageQualityField.getPreferredSize().height;

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addGroup(layout.createSequentialGroup()
            .addComponent(imgCompLabel)
            .addComponent(imgCompInfo)
        )
        .addComponent(imageQualityField)
        .addGroup(layout.createSequentialGroup()
            .addComponent(imgMinDimLabel)
            .addComponent(imgMinDimInfo)
        )
        .addComponent(imageMinDimensionField)
        .addGroup(layout.createSequentialGroup()
            .addComponent(imgMaxSizeLabel)
            .addComponent(imgMaxSizeInfo)
        )
        .addComponent(imageMaxSizeField)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addComponent(imgCompLabel)
            .addComponent(imgCompInfo)
        )
        .addComponent(imageQualityField, fh, fh, fh)
        .addGap(UIHelper.GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(imgMinDimLabel)
            .addComponent(imgMinDimInfo)
        )
        .addComponent(imageMinDimensionField, fh, fh, fh)
        .addGap(UIHelper.GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(imgMaxSizeLabel)
            .addComponent(imgMaxSizeInfo)
        )
        .addComponent(imageMaxSizeField, fh, fh, fh)
    );
    setLayout(layout);

    parent.addChangeListeners(imageQualityField, imageMinDimensionField, imageMaxSizeField);

  }

  @Override
  public void saveChanges(Map<String, Object> prefs) {
    String qualityStr = imageQualityField.getText();
    try {
      int quality = Integer.parseInt(qualityStr);
      if (quality < 1 || quality > 100) {
        throw new NumberFormatException();
      }
      prefs.put(DailyImagePoster.PREF_IMAGE_QUALITY, quality);
    } catch (NumberFormatException e) {
      DailyImagePoster.LOG.warn("Invalid input for image quality: {}", qualityStr);
    }
    String minDimStr = imageMinDimensionField.getText();
    try {
      int minDim = Integer.parseInt(minDimStr);
      if (minDim < 1) {
        throw new NumberFormatException();
      }
      prefs.put(DailyImagePoster.PREF_MIN_IMAGE_DIMENSION, minDim);
    } catch (NumberFormatException e) {
      DailyImagePoster.LOG.warn("Invalid input for min image dimension: {}", minDimStr);
    }
    String maxSizeStr = imageMaxSizeField.getText();
    try {
      int maxSize = Integer.parseInt(maxSizeStr);
      if (maxSize < 1) {
        throw new NumberFormatException();
      }
      prefs.put(DailyImagePoster.PREF_MAX_IMAGE_SIZE, maxSize);
    } catch (NumberFormatException e) {
      DailyImagePoster.LOG.warn("Invalid input for max image size: {}", maxSizeStr);
    }
  }

}
