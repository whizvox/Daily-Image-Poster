package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.util.StringHelper;
import me.whizvox.dailyimageposter.util.UIHelper;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Map;

import static me.whizvox.dailyimageposter.util.UIHelper.GAP_SIZE;

public class ImagesPrefsPanel extends AbstractPrefsPanel {

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
  private final JTextField thresholdField;
  private final JTextField waifu2xLocationField;
  private final JButton waifu2xLocationButton;
  private final JTextField waifu2xArgsField;

  public ImagesPrefsPanel(PreferencesPanel parent) {
    super(parent);
    DailyImagePoster app = DailyImagePoster.getInstance();

    JLabel imgCompLabel = new JLabel("Image Compression Quality");
    JLabel imgCompInfo = new JLabel("(Integer between 0 and 100)");
    UIHelper.setPlainFont(imgCompInfo);
    imageQualityField = new JTextField(String.valueOf(app.preferences.getInt(DailyImagePoster.PREF_IMAGE_QUALITY)));
    JLabel imgMinDimLabel = new JLabel("Minimum Image Dimension");
    JLabel imgMinDimInfo = new JLabel("(Positive Integer)");
    UIHelper.setPlainFont(imgMinDimInfo);
    imageMinDimensionField = new JTextField(String.valueOf(app.preferences.getInt(DailyImagePoster.PREF_MIN_IMAGE_DIMENSION)));
    JLabel imgMaxSizeLabel = new JLabel("Maximum Image Size (bytes)");
    JLabel imgMaxSizeInfo = new JLabel("(Positive Integer)");
    UIHelper.setPlainFont(imgMaxSizeInfo);
    imageMaxSizeField = new JTextField(String.valueOf(app.preferences.getInt(DailyImagePoster.PREF_MAX_IMAGE_SIZE)));
    JLabel hashAlgorithmLabel = new JLabel("Image Hashing Algorithm");
    hashAlgorithmCombo = new JComboBox<>(HASH_ALGORITHM_DISPLAYS);
    hashAlgorithmCombo.setSelectedIndex(getHashAlgorithmIndex(app.preferences.getString(DailyImagePoster.PREF_IMAGE_HASH_ALGORITHM)));
    JLabel bitResLabel = new JLabel("Image Hash Bit Resolution");
    JLabel bitResInfo = new JLabel("(Integer)");
    UIHelper.setPlainFont(bitResInfo);
    bitResField = new JTextField(Integer.toString(app.preferences.getInt(DailyImagePoster.PREF_IMAGE_HASH_BIT_RES)));
    JLabel thresholdLabel = new JLabel("Image Similarity Threshold");
    JLabel thresholdInfo = new JLabel("(Decimal between 0.0 and 1.0)");
    UIHelper.setPlainFont(thresholdInfo);
    thresholdField = new JTextField(Double.toString(app.preferences.getDouble(DailyImagePoster.PREF_SIMILARITY_THRESHOLD)));
    JLabel waifu2xLocationLabel = new JLabel("waifu2x Location");
    JLabel waifu2xLocationInfo = new JLabel("(Download page)");
    UIHelper.addHyperlink(waifu2xLocationInfo, "https://github.com/DeadSix27/waifu2x-converter-cpp/releases");
    waifu2xLocationField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_WAIFU2X_LOCATION));
    waifu2xLocationButton = new JButton("Browse");
    JLabel waifu2xArgsLabel = new JLabel("waifu2x Arguments");
    JLabel waifu2xArgsInfo = new JLabel("(-i and -o supplied automatically)");
    UIHelper.setPlainFont(waifu2xArgsInfo);
    waifu2xArgsField = new JTextField(app.preferences.getString(DailyImagePoster.PREF_WAIFU2X_ARGS));

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
        .addGroup(layout.createSequentialGroup()
            .addComponent(thresholdLabel)
            .addComponent(thresholdInfo)
        )
        .addComponent(thresholdField)
        .addGroup(layout.createSequentialGroup()
            .addComponent(waifu2xLocationLabel)
            .addComponent(waifu2xLocationInfo)
        )
        .addGroup(layout.createSequentialGroup()
            .addComponent(waifu2xLocationField)
            .addComponent(waifu2xLocationButton)
        )
        .addGroup(layout.createSequentialGroup()
            .addComponent(waifu2xArgsLabel)
            .addComponent(waifu2xArgsInfo)
        )
        .addComponent(waifu2xArgsField)
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
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(thresholdLabel)
            .addComponent(thresholdInfo)
        )
        .addComponent(thresholdField, fh, fh, fh)
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(waifu2xLocationLabel)
            .addComponent(waifu2xLocationInfo)
        )
        .addGroup(layout.createParallelGroup()
            .addComponent(waifu2xLocationField, fh, fh, fh)
            .addComponent(waifu2xLocationButton, fh, fh, fh)
        )
        .addGap(GAP_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(waifu2xArgsLabel)
            .addComponent(waifu2xArgsInfo)
        )
        .addComponent(waifu2xArgsField, fh, fh, fh)
    );
    setLayout(layout);

    parent.addChangeListeners(imageQualityField, imageMinDimensionField, imageMaxSizeField, bitResField, thresholdField,
        waifu2xLocationField, waifu2xArgsField);
    hashAlgorithmCombo.addActionListener(event -> parent.markUnsavedChanges());
    waifu2xLocationButton.addActionListener(e -> chooseWaifu2xLocation());
  }

  private void chooseWaifu2xLocation() {
    String currentLocation = waifu2xLocationField.getText();
    File parent;
    if (StringHelper.isNullOrBlank(currentLocation)) {
      parent = new File(".");
    } else {
      parent = new File(currentLocation).getParentFile();
    }
    JFileChooser fileChooser = new JFileChooser(parent);
    int choice = fileChooser.showOpenDialog(this);
    if (choice != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File selectedFile = fileChooser.getSelectedFile();
    // TODO Run the selected executable with --version to see if it's valid
    waifu2xLocationField.setText(selectedFile.getAbsolutePath());
  }

  @Override
  public void saveChanges(Map<String, Object> prefs) {
    saveInt(prefs, DailyImagePoster.PREF_IMAGE_QUALITY, imageQualityField, value -> value > 0 && value <= 100);
    saveInt(prefs, DailyImagePoster.PREF_MIN_IMAGE_DIMENSION, imageMinDimensionField, value -> value > 0);
    saveInt(prefs, DailyImagePoster.PREF_MAX_IMAGE_SIZE, imageMaxSizeField, value -> value > 0);
    prefs.put(DailyImagePoster.PREF_IMAGE_HASH_ALGORITHM, HASH_ALGORITHMS[hashAlgorithmCombo.getSelectedIndex()][1]);
    saveInt(prefs, DailyImagePoster.PREF_IMAGE_HASH_BIT_RES, bitResField, value -> value > 0 && value <= 1024);
    saveDouble(prefs, DailyImagePoster.PREF_SIMILARITY_THRESHOLD, thresholdField, value -> value >= 0.0 && value <= 1.0);
    prefs.put(DailyImagePoster.PREF_WAIFU2X_LOCATION, waifu2xLocationField.getText());
    prefs.put(DailyImagePoster.PREF_WAIFU2X_ARGS, waifu2xArgsField.getText());
  }

}
