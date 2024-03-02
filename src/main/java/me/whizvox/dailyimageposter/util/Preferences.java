package me.whizvox.dailyimageposter.util;

import me.whizvox.dailyimageposter.DailyImagePoster;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Preferences {

  private final Path filePath;
  private final Map<String, Object> defaultValues;
  private final Properties values;

  public Preferences(Path filePath, @Nullable Map<String, Object> defaultValues) {
    this.filePath = filePath;
    this.defaultValues = new HashMap<>();
    if (defaultValues != null) {
      this.defaultValues.putAll(defaultValues);
    }
    values = new Properties();
  }

  public Preferences(Path filePath) {
    this(filePath, null);
  }

  public void forEach(BiConsumer<String, Object> action) {
    values.forEach((key, value) -> action.accept((String) key, value));
  }

  public void save() {
    try (OutputStream out = Files.newOutputStream(filePath)) {
      values.store(out, null);
      DailyImagePoster.LOG.info("Saved preferences to {}", filePath);
    } catch (IOException e) {
      DailyImagePoster.LOG.debug("Could not save preferences to " + filePath, e);
    }
  }

  public void load() {
    values.clear();
    if (Files.exists(filePath)) {
      try (InputStream in = Files.newInputStream(filePath, StandardOpenOption.CREATE)) {
        values.load(in);
        DailyImagePoster.LOG.info("Loaded preferences from {}", filePath);
      } catch (IOException e) {
        DailyImagePoster.LOG.warn("Could not load preferences from " + filePath, e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key, Function<String, T> decoder) {
    String value = values.getProperty(key);
    if (value != null) {
      try {
        return decoder.apply(value);
      } catch (Exception e) {
        DailyImagePoster.LOG.warn("Could not parse value for preference " + key, e);
      }
    }
    return (T) defaultValues.get(key);
  }

  public String getString(String key) {
    return get(key, s -> s);
  }

  public boolean getBoolean(String key) {
    return get(key, Boolean::parseBoolean);
  }

  public int getInt(String key) {
    return get(key, Integer::parseInt);
  }

  public LocalDateTime getDateTime(String key) {
    return get(key, s -> LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(s)));
  }

  public <T> void set(String key, T value, Function<T, String> encoder) {
    Object defValue = defaultValues.get(key);
    if (Objects.equals(defValue, value)) {
      values.remove(key);
    } else {
      values.setProperty(key, encoder.apply(value));
    }
  }

  public void setString(String key, String value) {
    set(key, value, s -> s);
  }

  public void setBoolean(String key, boolean value) {
    set(key, value, b -> Boolean.toString(b));
  }

  public void setInt(String key, int value) {
    set(key, value, i -> Integer.toString(i));
  }

  public void setDateTime(String key, LocalDateTime value) {
    set(key, value, DateTimeFormatter.ISO_DATE_TIME::format);
  }

  public void setObject(String key, Object value) {
    if (value instanceof String s) {
      setString(key, s);
    } else if (value instanceof Boolean b) {
      setBoolean(key, b);
    } else if (value instanceof Integer i) {
      setInt(key, i);
    } else if (value instanceof Short s) {
      setInt(key, s);
    } else if (value instanceof Byte b) {
      setInt(key, b);
    } else if (value instanceof LocalDateTime ldt) {
      setDateTime(key, ldt);
    } else {
      throw new IllegalArgumentException("Unknown value type: " + (value == null ? "null" : value.getClass()));
    }
  }

  public boolean remove(String key) {
    return values.remove(key) != null;
  }

}
