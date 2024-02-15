package me.whizvox.dailyimageposter.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StringHelper {

  private static final String[] UNITS = new String[] { "B", "KB", "MB", "GB", "TB", "PB" };

  public static String formatBytesLength(long n) {
    if (n < 1000) {
      return n + " B";
    }
    int unitIndex = 0;
    float scaledLength = n;
    do {
      scaledLength /= 1000.0F;
      unitIndex++;
    } while (scaledLength > 1000.0F && unitIndex < UNITS.length);
    return String.format("%.1f %s", scaledLength, UNITS[unitIndex]);
  }

  private static final DateTimeFormatter COMPACT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddkkmmss");

  public static String formatCompactTimestamp(LocalDateTime ldt) {
    return COMPACT_DATE_TIME_FORMATTER.format(ldt);
  }

  public static LocalDateTime parseCompactTimestamp(String s) {
    return LocalDateTime.from(COMPACT_DATE_TIME_FORMATTER.parse(s));
  }

  public static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      sb.append(String.format("%02x", b & 0xFF));
    }
    return sb.toString();
  }

}
