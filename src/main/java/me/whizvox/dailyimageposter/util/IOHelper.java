package me.whizvox.dailyimageposter.util;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
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

  private static BufferedImage prepareImage(BufferedImage image, int width, int height) {
    boolean resize = width != image.getWidth() || height != image.getHeight();
    // image won't save as JPEG if color space has alpha component
    if (!image.getColorModel().hasAlpha() && !resize) {
      return image;
    }
    BufferedImage target = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = target.createGraphics();
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, width, height);
    if (resize) {
      g.drawImage(image, 0, 0, width, height, null);
    } else {
      g.drawImage(image, 0, 0, null);
    }
    g.dispose();
    return target;
  }

  /**
   * Save an image as a JPEG while allowing control of the compression quality.
   * @param image Source image
   * @param dest Destination image path
   * @param quality A value between 0 and 1
   * @throws IOException If image reading or writing failed
   */
  public static void saveAsJpeg(BufferedImage image, Path dest, int width, int height, float quality) throws IOException {
    if (quality < 0.01F || quality > 1.0F) {
      throw new IllegalArgumentException("Quality must be between 0 and 1: " + quality);
    }
    JPEGImageWriteParam param = new JPEGImageWriteParam(Locale.getDefault());
    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(quality);
    ImageWriter writer = ImageIO.getImageWritersBySuffix("jpg").next();
    try (FileImageOutputStream out = new FileImageOutputStream(dest.toFile())) {
      writer.setOutput(out);
      IIOImage iioImg = new IIOImage(prepareImage(image, width, height), null, null);
      writer.write(null, iioImg, param);
    }
    writer.dispose();
  }

}
