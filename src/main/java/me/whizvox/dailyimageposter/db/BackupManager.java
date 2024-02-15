package me.whizvox.dailyimageposter.db;

import com.fasterxml.jackson.core.type.TypeReference;
import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.util.IOHelper;
import me.whizvox.dailyimageposter.util.JsonHelper;
import me.whizvox.dailyimageposter.util.StringHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BackupManager {

  private static final String META_FILE_NAME = "meta.json";

  private final Path root;
  private final Map<String, BackupEntry> meta;

  public BackupManager(Path root) {
    this.root = root;
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    meta = new HashMap<>();
    try {
      if (Files.exists(root.resolve(META_FILE_NAME))) {
        readMetaData();
      } else {
        buildMetaData();
      }
    } catch (IOException e) {
      DailyImagePoster.LOG.warn("Could not initialize backup metadata", e);
    }
  }

  private void addMetaData(Path path) {
    String hash = IOHelper.sha1(path);
    BackupEntry entry = new BackupEntry(path.getFileName().toString(), hash, LocalDateTime.now());
    meta.put(entry.fileName, entry);
  }

  private void readMetaData() throws IOException {
    meta.clear();
    try (InputStream in = Files.newInputStream(root.resolve(META_FILE_NAME))) {
      List<BackupEntry> entries = JsonHelper.OBJECT_MAPPER.readValue(in, new TypeReference<>() {});
      entries.forEach(entry -> meta.put(entry.fileName, entry));
    }
    buildMetaData(true);
  }

  private void buildMetaData(boolean skipKnownFiles) throws IOException {
    if (!skipKnownFiles) {
      meta.clear();
    }
    try (Stream<Path> stream = Files.walk(root, 1)) {
      stream
          .filter(path -> !path.equals(root) && !path.getFileName().toString().equals(META_FILE_NAME) && (!skipKnownFiles || !meta.containsKey(path.getFileName().toString())))
          .forEach(this::addMetaData);
    }
    saveMetaData();
  }

  public void buildMetaData() throws IOException {
    buildMetaData(false);
  }

  public void saveMetaData() throws IOException {
    try (OutputStream out = Files.newOutputStream(root.resolve(META_FILE_NAME))) {
      List<BackupEntry> entries = meta.values().stream()
          .sorted(Comparator.comparing(o -> o.timestamp))
          .toList();
      JsonHelper.OBJECT_MAPPER.writeValue(out, entries);
    }
  }

  @Nullable
  public Path createBackup(Path origFile, boolean force) throws IOException {
    String hash = IOHelper.sha1(origFile);
    if (!force && meta.values().stream().anyMatch(entry -> entry.sha1Hash.equals(hash))) {
      return null;
    }
    String backupName = StringHelper.formatCompactTimestamp(LocalDateTime.now()) + "_" + origFile.getFileName().toString();
    Path backupFile = root.resolve(Paths.get(backupName));
    Files.copy(origFile, backupFile);
    meta.put(backupName, new BackupEntry(backupName, hash, LocalDateTime.now()));
    return backupFile;
  }

  public int cleanupOldBackups(String origFileName, int max) throws IOException {
    List<Path> backups;
    try (Stream<Path> stream = Files.walk(root, 1)) {
      backups = stream
          .filter(path -> path.getFileName().toString().endsWith(origFileName))
          .toList();
    }
    int overflow = backups.size() - max;
    if (overflow <= 0) {
      return 0;
    }
    backups.stream()
        .sorted((o1, o2) -> {
          int ldt1 = Integer.parseInt(o1.getFileName().toString().substring(0, 14));
          int ldt2 = Integer.parseInt(o2.getFileName().toString().substring(0, 14));
          return Integer.compare(ldt1, ldt2);
        })
        .limit(overflow)
        .forEach(path -> {
          try {
            Files.delete(path);
            DailyImagePoster.LOG.debug("Deleted backup {}", path);
          } catch (IOException e) {
            DailyImagePoster.LOG.warn("Could not delete backup " + path, e);
          }
        });
    return overflow;
  }

}
