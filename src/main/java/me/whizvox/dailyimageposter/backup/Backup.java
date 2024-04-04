package me.whizvox.dailyimageposter.backup;

import java.time.LocalDateTime;

public record Backup(String fileName, String origFileName, String sha1, LocalDateTime created) {
}
