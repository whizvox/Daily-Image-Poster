package me.whizvox.dailyimageposter.backup;

import me.whizvox.dailyimageposter.util.IOHelper;
import me.whizvox.dailyimageposter.util.StringHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;

public class BackupService {

  private final Path root;
  private final BackupRepository repo;

  public BackupService(Path root, BackupRepository repo) {
    this.root = root;
    this.repo = repo;
    IOHelper.mkdirs(root);
  }

  public Path resolve(String fileName) {
    return root.resolve(fileName);
  }

  public void forEach(Consumer<Path> consumer) {
    try (var walk = Files.walk(root, 1).filter(p -> !p.equals(root))) {
      walk.forEach(consumer);
    } catch (IOException e) {
      LOG.warn("Could not perform walk of " + root, e);
    }
  }

  public BackupIntegrityReport verifyIntegrity(boolean attemptImport, boolean updateMismatches) {
    List<String> mismatchedHashes = new ArrayList<>();
    List<String> notInDatabase = new ArrayList<>();
    List<String> notInFilesystem = new ArrayList<>();
    List<String> imported = new ArrayList<>();
    List<String> failedToImport = new ArrayList<>();
    List<String> updatedHashes = new ArrayList<>();
    Set<String> knownFiles = new HashSet<>();
    forEach(path -> {
      String fileName = path.getFileName().toString();
      knownFiles.add(fileName);
      Backup backup = repo.get(fileName);
      if (backup == null) {
        if (attemptImport) {
          boolean success = false;
          int index = fileName.indexOf('_');
          if (index >= 0) {
            String timestamp = fileName.substring(0, index);
            try {
              LocalDateTime created = StringHelper.parseCompactTimestamp(timestamp);
              String origFileName = fileName.substring(index + 1);
              String hash = IOHelper.sha1(path);
              repo.add(fileName, origFileName, hash, created);
              success = true;
            } catch (DateTimeException ignored) {}
          }
          if (success) {
            imported.add(fileName);
          } else {
            failedToImport.add(fileName);
          }
        } else {
          notInDatabase.add(fileName);
        }
      } else {
        String hash = IOHelper.sha1(path);
        if (!hash.equals(backup.sha1())) {
          if (updateMismatches) {
            repo.updateHash(fileName, hash);
            updatedHashes.add(fileName);
          } else {
            mismatchedHashes.add(fileName);
          }
        }
      }
    });
    repo.forEach(backup -> {
      if (!knownFiles.contains(backup.fileName())) {
        notInFilesystem.add(backup.fileName());
      }
    });
    return new BackupIntegrityReport(mismatchedHashes, notInDatabase, notInFilesystem, imported, failedToImport,
        updatedHashes);
  }

  @Nullable
  public String createBackup(Path path) {
    String hash = IOHelper.sha1(path);
    Backup backup = repo.getByHash(hash);
    if (backup != null) {
      LOG.debug("Skipping creating backup of {}, found identical hash", path);
      return null;
    }
    String fileName = path.getFileName().toString();
    String backupFileName = StringHelper.formatCompactTimestamp(LocalDateTime.now()) + "_" + fileName;
    Path backupFile = root.resolve(backupFileName);
    try {
      Files.copy(path, backupFile);
    } catch (IOException e) {
      throw new RuntimeException("Could not create backup copy of " + path + " to " + backupFile, e);
    }
    repo.add(backupFileName, fileName, hash, LocalDateTime.now());
    LOG.info("Created backup of {} at {}", path, backupFile);
    return backupFileName;
  }

  public void cleanupOldBackups(String origFileName, int max) {
    repo.getAllOf(origFileName).stream()
        .sorted(Comparator.comparing(Backup::created).reversed())
        .skip(max)
        .forEach(backup -> {
          LOG.debug("Deleting old backup {}", backup.fileName());
          //repo.delete(backup.fileName());
        });
  }

}
