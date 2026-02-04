package me.whizvox.dailyimageposter.reserve;

import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.util.StringHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReserveManager {

  private final Path root;
  private final ReserveRepository repo;

  public ReserveManager(Path root, ReserveRepository repo) {
    this.root = root;
    this.repo = repo;
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new RuntimeException("Could not create directory for reserves: " + root, e);
    }
  }

  public ReserveRepository getRepo() {
    return repo;
  }

  public Path getPath(String fileName) {
    return root.resolve(fileName);
  }

  public String copy(Path originalFilePath) throws IOException {
    String origFileName = originalFilePath.getFileName().toString();
    Path dst = getPath(origFileName);
    int copyIndex = 0;
    while (Files.exists(dst)) {
      copyIndex++;
      String[] baseAndExt = StringHelper.getFileNameBaseAndExtension(origFileName);
      dst = getPath(baseAndExt[0] + " (" + copyIndex + ")" + baseAndExt[1]);
    }
    Files.copy(originalFilePath, dst);
    return dst.getFileName().toString();
  }

  public void delete(String fileName) {
    Path path = getPath(fileName);
    try {
      Files.deleteIfExists(path);
      repo.deleteByFile(fileName);
    } catch (IOException e) {
      DailyImagePoster.LOG.warn("Could not delete reserve image {}", path, e);
    }
  }

}
