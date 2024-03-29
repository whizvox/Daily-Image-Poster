package me.whizvox.dailyimageposter.db;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import me.whizvox.dailyimageposter.DailyImagePoster;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ImageManager {

  private final Path root;
  private HashingAlgorithm hasher;
  private final ImageHashRepository hashRepo;

  public ImageManager(Path root, HashingAlgorithm hasher, ImageHashRepository hashRepo) {
    this.root = root;
    this.hasher = hasher;
    this.hashRepo = hashRepo;
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Path getRoot() {
    return root;
  }

  public void setHashingAlgorithm(HashingAlgorithm hasher) {
    this.hasher = hasher;
  }

  public Path getImagePath(String fileName) {
    return root.resolve(fileName);
  }

  @Nullable
  public Path getImagePath(Post post) {
    if (post.fileName() == null) {
      return null;
    }
    return getImagePath(post.fileName());
  }

  public void copy(Path origImageFile, Post post) throws IOException {
    String prefix = String.format("%04d_", post.number());
    Path dstFile;
    if (origImageFile.getFileName().toString().startsWith(prefix)) {
      dstFile = root.resolve(post.fileName());
    } else {
      dstFile = root.resolve(prefix + post.fileName());
    }
    Files.copy(origImageFile, dstFile, StandardCopyOption.REPLACE_EXISTING);
  }

  public void forEach(Consumer<Path> consumer) {
    try (var walk = Files.walk(root, 1)) {
      walk.forEach(path -> {
        if (!path.equals(root)) {
          consumer.accept(path);
        }
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Hash hashImage(Path path) {
    try {
      return hasher.hash(path.toFile());
    } catch (IOException e) {
      throw new RuntimeException("Could not create hash of image " + path, e);
    }
  }

  public Hash hashImage(String fileName) {
    return hashImage(getImagePath(fileName));
  }

  public void hashAndAddImage(String fileName) {
    Hash hash = hashImage(fileName);
    hashRepo.addOrUpdate(fileName, hash);
  }

  public void updateImageHashes(boolean force) {
    forEach(path -> {
      String fileName = path.getFileName().toString();
      if (force) {
        DailyImagePoster.LOG.debug("Updating image hash by force: " + path);
        hashRepo.addOrUpdate(fileName, hashImage(path));
      } else if (!hashRepo.exists(fileName)) {
        DailyImagePoster.LOG.debug("Updating image hash: " + path);
        hashRepo.add(fileName, hashImage(path));
      }
    });
  }

  public List<SimilarImage> findSimilarImages(Path imagePath, double threshold) {
    List<SimilarImage> similarImages = new ArrayList<>();
    Hash origHash = hashImage(imagePath);
    hashRepo.forEach(entry -> {
      double similarity = origHash.normalizedHammingDistance(entry.hash());
      if (similarity <= threshold) {
        similarImages.add(new SimilarImage(entry.fileName(), similarity));
      }
    });
    return similarImages;
  }

  public record SimilarImage(String fileName, double similarity) {
  }

}
