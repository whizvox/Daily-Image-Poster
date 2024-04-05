package me.whizvox.dailyimageposter.image;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import me.whizvox.dailyimageposter.post.Post;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

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

  public String copy(Path origImageFile, int number) throws IOException {
    String fileName = origImageFile.getFileName().toString();
    String prefix = String.format("%04d_", number);
    boolean addPrefix = !fileName.startsWith(prefix);
    int copy = 0;
    Path dstFile;
    do {
      dstFile = root.resolve(
          (addPrefix ? prefix : "") +
          fileName +
          (copy == 0 ? "" : " (" + copy + ")")
      );
      copy++;
    } while (Files.exists(dstFile));
    Files.copy(origImageFile, dstFile);
    return dstFile.getFileName().toString();
  }

  public <T> T applyStream(Function<Stream<Path>, T> func) {
    try (var stream = Files.list(root).filter(path -> !path.equals(root))) {
      return func.apply(stream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void consumeStream(Consumer<Stream<Path>> consumer) {
    applyStream(stream -> {
      consumer.accept(stream);
      return null;
    });
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

  private boolean addImageHash(Path path, boolean forceUpdate) {
    String fileName = path.getFileName().toString();
    boolean exists = hashRepo.exists(fileName);
    if (exists) {
      if (forceUpdate) {
        hashRepo.update(fileName, hashImage(path));
        return true;
      }
    } else {
      hashRepo.add(fileName, hashImage(path));
      return true;
    }
    return false;
  }

  public boolean addImageHash(String fileName, boolean forceUpdate) {
    return addImageHash(getImagePath(fileName), forceUpdate);
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
