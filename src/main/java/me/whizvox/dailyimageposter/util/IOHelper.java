package me.whizvox.dailyimageposter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IOHelper {

  public static InputStream getResourceStream(String name) {
    return IOHelper.class.getClassLoader().getResourceAsStream(name);
  }

  public static URL getResource(String name) {
    return IOHelper.class.getClassLoader().getResource(name);
  }

  public static String sha1(Path path, int bufferSize) {
    try (InputStream in = Files.newInputStream(path)) {
      MessageDigest sha1 = MessageDigest.getInstance("SHA1");
      byte[] buffer = new byte[bufferSize];
      int read;
      while ((read = in.read(buffer)) != -1) {
        sha1.update(buffer, 0, read);
      }
      return StringHelper.bytesToHex(sha1.digest());
    } catch (NoSuchAlgorithmException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String sha1(Path path) {
    return sha1(path, 4096); // 4 KB
  }

}
