package me.whizvox.dailyimageposter.util;

import me.whizvox.dailyimageposter.reddit.pojo.Comment;
import org.jetbrains.annotations.Nullable;
import org.stringtemplate.v4.ST;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  public static boolean isNullOrBlank(String s) {
    return s == null || s.isBlank();
  }

  public static String nullIfBlank(String s) {
    return s == null || s.isBlank() ? null : s;
  }

  public static String withCutoff(String s, int maxLength) {
    if (s.length() < maxLength) {
      return s;
    }
    return s.substring(0, maxLength - 3) + "...";
  }

  public static String responseToString(HttpResponse<?> response) {
    return response.uri() + " -> [" + response.statusCode() + "] " + response.body();
  }

  public static String[] getFileNameBaseAndExtension(String fileName) {
    int indexOf = fileName.lastIndexOf('.');
    if (indexOf != -1) {
      return new String[] { fileName.substring(0, indexOf), fileName.substring(indexOf) };
    }
    return new String[] { fileName, "" };
  }

  public static String getFileNameWithoutExtension(String fileName) {
    return getFileNameBaseAndExtension(fileName)[0];
  }

  public static String getFileNameExtension(String fileName) {
    return getFileNameBaseAndExtension(fileName)[1];
  }

  private static final Pattern PATTERN_SUBMISSION_URL = Pattern.compile("https://www\\.reddit\\.com/r/(\\w+)/comments/(\\w+)/(\\w*)");

  /**
   * Split a Reddit submission URL into 3 parts: 1) subreddit name, 2) link ID, 3) slug.
   * @param submissionUrl The submission URL. MUST be formatted like so:
   *                      <code>https://www.reddit.com/r/&lt;subreddit>/comments/&lt;linkId>/&lt;slug></code>.
   * @return A string array containing each part. <code>[0]</code> = subreddit name, <code>[1]</code> = link ID,
   * <code>[2]</code> = slug
   */
  public static String[] getRedditLinkParts(String submissionUrl) {
    Matcher matcher = PATTERN_SUBMISSION_URL.matcher(submissionUrl);
    if (matcher.find()) {
      return new String[] { matcher.group(1), matcher.group(2), matcher.group(3) };
    }
    return new String[0];
  }

  @Nullable
  public static String getRedditLinkId(String submissionUrl) {
    String[] parts = getRedditLinkParts(submissionUrl);
    if (parts.length == 0) {
      return null;
    }
    return parts[1];
  }

  @Nullable
  public static Comment parseCommentFromJQuery(String jqueryResponse) {
    int i0 = jqueryResponse.indexOf("[18, 19, \"call\", [[");
    int i1 = jqueryResponse.indexOf("], false]], [0, 20, \"call\"");
    if (i0 < 0 || i1 < 0) {
      return null;
    }
    return JsonHelper.read(jqueryResponse.substring(i0 + 19, i1), Comment.class);
  }

  public static String renderFromTemplate(String template, Map<String, Object> args) {
    ST st = new ST(template);
    args.forEach(st::add);
    // ST, for some dumb reason, adds carriage returns to all new lines
    return st.render().replaceAll("\r", "");
  }

}
