package me.whizvox.dailyimageposter.db;

import java.time.LocalDateTime;

public class BackupEntry {

  public String fileName;
  public String sha1Hash;
  public LocalDateTime timestamp;

  public BackupEntry(String fileName, String sha1Hash, LocalDateTime timestamp) {
    this.fileName = fileName;
    this.sha1Hash = sha1Hash;
    this.timestamp = timestamp;
  }

  public BackupEntry() {
  }

}
