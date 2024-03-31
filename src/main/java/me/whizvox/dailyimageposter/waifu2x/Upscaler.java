package me.whizvox.dailyimageposter.waifu2x;

import me.whizvox.dailyimageposter.DailyImagePoster;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Upscaler {

  private final Path path;
  private final String args;

  public Upscaler(Path waifu2xExecutable, String args) {
    this.path = waifu2xExecutable;
    this.args = args;
  }

  public void run(Path inputImage, Path output, Consumer<Boolean> onComplete) {
    List<String> cmd = new ArrayList<>();
    cmd.add(path.toAbsolutePath().toString());
    cmd.add("-i");
    cmd.add(inputImage.toAbsolutePath().toString());
    cmd.add("-o");
    cmd.add(output.toAbsolutePath().toString());
    Collections.addAll(cmd, args.split(" "));
    DailyImagePoster.LOG.debug("Running command: {}", cmd);
    ProcessBuilder builder = new ProcessBuilder(cmd)
        .redirectErrorStream(true)
        .directory(path.getParent().toFile());
    Executors.newCachedThreadPool().submit(() -> {
      try {
        Process process = builder.start();
        try (BufferedReader outReader = process.inputReader()) {
          String line;
          // effectively waits for the process to complete
          while ((line = outReader.readLine()) != null) {
            DailyImagePoster.LOG.debug("[CMDOUT] {}", line);
          }
        }
        try (BufferedReader reader = process.errorReader()) {
          String line;
          while ((line = reader.readLine()) != null) {
            DailyImagePoster.LOG.debug("[CMDERR] {}", line);
          }
        }
        onComplete.accept(true);
      } catch (IOException e) {
        DailyImagePoster.LOG.warn("Error while running command", e);
        onComplete.accept(false);
      }
    });

  }

}
