package me.whizvox.dailyimageposter.backup;

import java.util.Collections;
import java.util.List;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;

public record BackupIntegrityReport(List<String> mismatchedHashes,
                                    List<String> notInDatabase,
                                    List<String> notInFilesystem,
                                    List<String> imported,
                                    List<String> failedToImport,
                                    List<String> updatedHashes) {

  public BackupIntegrityReport(List<String> mismatchedHashes,
                               List<String> notInDatabase,
                               List<String> notInFilesystem,
                               List<String> imported,
                               List<String> failedToImport,
                               List<String> updatedHashes) {
    this.mismatchedHashes = Collections.unmodifiableList(mismatchedHashes);
    this.notInDatabase = Collections.unmodifiableList(notInDatabase);
    this.notInFilesystem = Collections.unmodifiableList(notInFilesystem);
    this.imported = Collections.unmodifiableList(imported);
    this.failedToImport = Collections.unmodifiableList(failedToImport);
    this.updatedHashes = Collections.unmodifiableList(updatedHashes);
  }

  public void log() {
    mismatchedHashes.forEach(s -> LOG.warn("Found SHA1 hash mismatch: {}", s));
    notInDatabase.forEach(s -> LOG.warn("Found backup not in database: {}", s));
    notInFilesystem.forEach(s -> LOG.warn("Found backup not in filesystem: {}", s));
    failedToImport.forEach(s -> LOG.warn("Failed to import backup from filesystem: {}", s));
    imported.forEach(s -> LOG.info("Successfully imported backup from filesystem: {}", s));
    updatedHashes.forEach(s -> LOG.info("Successfully updated SHA1 hash of backup: {}", s));
  }

}
