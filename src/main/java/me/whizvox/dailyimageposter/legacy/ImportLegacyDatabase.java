package me.whizvox.dailyimageposter.legacy;

import com.fasterxml.jackson.core.type.TypeReference;
import me.whizvox.dailyimageposter.DailyImagePoster;
import me.whizvox.dailyimageposter.db.Post;
import me.whizvox.dailyimageposter.util.JsonHelper;

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

  public void importLegacy(Path file) {
    try {
      List<Entry> entries = JsonHelper.OBJECT_MAPPER.readValue(file.toFile(), new TypeReference<>() {});
      app.getPosts().deleteAll();
      entries.forEach(entry -> {
        app.getPosts().add(new Post(UUID.randomUUID(), entry.id, (byte) 0, entry.title, entry.artist, entry.source, null, false, false, entry.redditPostId, null, entry.imgurId, null));
      });
    } catch (IOException e) {
      DailyImagePoster.LOG.warn("Could not import legacy database", e);
    }
  }

}
