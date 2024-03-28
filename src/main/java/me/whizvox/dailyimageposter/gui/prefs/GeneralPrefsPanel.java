package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class GeneralPrefsPanel extends AbstractPrefsPanel {

  private static final String[][] HASH_ALGORITHMS = {
      {"Perceptive (default)", "perceptive"},
      {"Average", "average"},
      {"Average Color", "averageColor"},
      {"Difference", "difference"},
      {"Wavelet", "wavelet"},
      {"Median", "median"},
      {"Average Kernel", "averageKernel"},
      {"Rotational Average", "rotAverage"},
      {"Rotational pHash", "rotP"},
      {"Hog (experimental)", "hog"}
  };
  private static final String[] HASH_ALGORITHM_DISPLAYS;

  static {
    HASH_ALGORITHM_DISPLAYS = new String[HASH_ALGORITHMS.length];
    for (int i = 0; i < HASH_ALGORITHMS.length; i++) {
      HASH_ALGORITHM_DISPLAYS[i] = HASH_ALGORITHMS[i][0];
    }
  }

  private static int getHashAlgorithmIndex(String algorithm) {
    for (int i = 0; i < HASH_ALGORITHMS.length; i++) {
      if (HASH_ALGORITHMS[i][1].equals(algorithm)) {
        return i;
      }
    }
    return 0;
  }

  private final JTextField imageQualityField;
  private final JTextField imageMinDimensionField;
  private final JTextField imageMaxSizeField;
  private final JComboBox<String> hashAlgorithmCombo;
  private final JTextField bitResField;

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
    JLabel hashAlgorithmLabel = new JLabel("Image Hashing Algorithm");
    hashAlgorithmCombo = new JComboBox<>(HASH_ALGORITHM_DISPLAYS);
    hashAlgorithmCombo.setSelectedIndex(getHashAlgorithmIndex(app.preferences.getString(DailyImagePoster.PREF_IMGHASH_ALGORITHM)));
    JLabel bitResLabel = new JLabel("Image Hash Bit Resolution");
    bitResField = new JTextField(Integer.toString(app.preferences.getInt(DailyImagePoster.PREF_IMGHASH_BIT_RESOLUTION)));

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
        .addComponent(hashAlgorithmLabel)
        .addComponent(hashAlgorithmCombo)
        .addComponent(bitResLabel)
        .addComponent(bitResField)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addComponent(imgCompLabel)
            .addComponent(imgCompInfo)
        )
        .addComponent(imageQualityField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(imgMinDimLabel)
            .addComponent(imgMinDimInfo)
        )
        .addComponent(imageMinDimensionField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(imgMaxSizeLabel)
            .addComponent(imgMaxSizeInfo)
        )
        .addComponent(imageMaxSizeField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(hashAlgorithmLabel)
        .addComponent(hashAlgorithmCombo, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addComponent(bitResLabel)
        .addComponent(bitResField, fh, fh, fh)
    );
    setLayout(layout);

    parent.addChangeListeners(imageQualityField, imageMinDimensionField, imageMaxSizeField, bitResField);
    hashAlgorithmCombo.addActionListener(event -> parent.markUnsavedChanges());
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
    String algorithm = HASH_ALGORITHMS[hashAlgorithmCombo.getSelectedIndex()][1];
    prefs.put(DailyImagePoster.PREF_IMGHASH_ALGORITHM, algorithm);
    String bitResStr = bitResField.getText();
    try {
      int bitRes = Integer.parseInt(bitResStr);
      if (bitRes < 1 || bitRes > 1024) {
        throw new NumberFormatException();
      }
      prefs.put(DailyImagePoster.PREF_IMGHASH_BIT_RESOLUTION, bitRes);
    } catch (NumberFormatException e) {
      DailyImagePoster.LOG.warn("Invalid input for image hash bit resolution: " + bitResStr);
    }
  }

}
