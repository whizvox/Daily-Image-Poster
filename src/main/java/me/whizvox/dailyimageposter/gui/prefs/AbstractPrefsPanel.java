package me.whizvox.dailyimageposter.gui.prefs;

import me.whizvox.dailyimageposter.DailyImagePoster;

import javax.swing.*;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractPrefsPanel extends JPanel {

  protected final PreferencesPanel parent;

  public AbstractPrefsPanel(PreferencesPanel parent) {
    this.parent = parent;
  }

  public abstract void saveChanges(Map<String, Object> prefs);

  protected <T> void save(Map<String, Object> prefs, String key, JTextField field, Function<String, T> parser, Predicate<T> validator) {
    String valueStr = field.getText();
    try {
      T value = parser.apply(valueStr);
      if (validator.test(value)) {
        prefs.put(key, value);
      } else {
        DailyImagePoster.LOG.warn("Invalid input for " + key + ": " + valueStr);
      }
    } catch (RuntimeException e) {
      DailyImagePoster.LOG.warn("Invalid input for " + key + ": " + valueStr, e);
    }
  }

  protected void saveInt(Map<String, Object> prefs, String key, JTextField field, Predicate<Integer> validator) {
    save(prefs, key, field, Integer::parseInt, validator);
  }

  protected void saveDouble(Map<String, Object> prefs, String key, JTextField field, Predicate<Double> validator) {
    save(prefs, key, field, Double::parseDouble, validator);
  }

}
