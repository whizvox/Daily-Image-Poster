package me.whizvox.dailyimageposter.legacy;

import com.fasterxml.jackson.core.type.TypeReference;
import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.db.Post;
import me.whizvox.dailyimageposter.util.JsonHelper;
import me.whizvox.dailyimageposter.util.StringHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class ImportLegacyDatabase {

  public static class Entry {
    public int id;
    public String fileName;
    public String title;
    public String artist;
    public String source;
    public String imgurId;
    public String redditPostId;
  }

  private final DailyImagePoster app;

  public ImportLegacyDatabase() {
    app = DailyImagePoster.getInstance();
  }

  public void importLegacy(Path legacyDir, boolean ignoreExistingEntries) {
    try {
      Path imagesDir = legacyDir.resolve("images");
      List<Entry> entries = JsonHelper.OBJECT_MAPPER.readValue(legacyDir.resolve("history.json").toFile(), new TypeReference<>() {});
      if (!ignoreExistingEntries) {
        app.getPosts().deleteAll();
      }
      entries.forEach(entry -> {
        Path imageFile = legacyDir.resolve(entry.fileName);
        String fileName = imageFile.getFileName().toString();
        if (ignoreExistingEntries) {
          Post existingPost = DailyImagePoster.getInstance().getPosts().getByFileName(fileName);
          if (existingPost != null) {
            DailyImagePoster.LOG.trace("Skipping already existing entry {}", fileName);
            return;
          }
        }
        Post post = new Post(UUID.randomUUID(), fileName, entry.id, (byte) 0, entry.title, entry.artist, entry.source, null, false, false, entry.redditPostId, null, entry.imgurId, null);
        app.getPosts().add(post);
        DailyImagePoster.LOG.debug("Added post to database: {} ({})", post.formatNumber(), StringHelper.withCutoff(post.fileName(), 40));
        try {
          app.images().copy(imagesDir.resolve(fileName), post);
        } catch (IOException e) {
          DailyImagePoster.LOG.warn("Could not import image " + fileName, e);
        }
      });
    } catch (IOException e) {
      DailyImagePoster.LOG.warn("Could not import legacy database", e);
    }
  }

}
