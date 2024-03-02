package me.whizvox.dailyimageposter.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

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

  /**
   * Save an image as a JPEG while allowing control of the compression quality.
   * @param source Source image path
   * @param dest Destination image path
   * @param quality A value between 0 and 1
   * @throws IOException If image reading or writing failed
   */
  public static void saveAsJpeg(Path source, Path dest, float quality) throws IOException {
    if (quality < 0.01F || quality > 1.0F) {
      throw new IllegalArgumentException("Quality must be between 0 and 1: " + quality);
    }
    BufferedImage image = ImageIO.read(source.toFile());
    String[] fileNameParts = StringHelper.getFileNameBaseAndExtension(source.getFileName().toString());
    JPEGImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
    param.setCompressionQuality(quality);
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    ImageWriter writer = ImageIO.getImageWritersBySuffix("jpg").next();
    try (FileImageOutputStream out = new FileImageOutputStream(dest.toFile())) {
      writer.setOutput(out);
      IIOImage image2 = new IIOImage(image, null, null);
      writer.write(null, image2, param);
    }
    writer.dispose();
  }

}
