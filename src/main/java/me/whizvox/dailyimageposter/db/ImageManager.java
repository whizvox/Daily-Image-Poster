package me.whizvox.dailyimageposter.db;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ImageManager {

  private final Path root;

  public ImageManager(Path root) {
    this.root = root;
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  public Path getImagePath(Post post) {
    if (post.fileName() == null) {
      return null;
    }
    return root.resolve(post.fileName());
  }

  public void copy(Path origImageFile, Post post) throws IOException {
    String prefix = String.format("%04d_", post.number());
    Path dstFile;
    if (origImageFile.getFileName().toString().startsWith(prefix)) {
      dstFile = root.resolve(post.fileName());
    } else {
      dstFile = root.resolve(prefix + post.fileName());
    }
    Files.copy(origImageFile, dstFile);
  }

  public void forEach(Consumer<Path> consumer) {
    try (var walk = Files.walk(root, 1)) {
      walk.forEach(consumer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
