package me.whizvox.dailyimageposter.reddit;

import java.util.HashMap;
import java.util.Map;

public class SubmitOptions {

  public enum Kind {
    LINK,
    SELF,
    IMAGE,
    VIDEO,
    VIDEOGIF;
    private final String name;
    Kind() {
      name = super.toString().toLowerCase();
    }
    public String getName() {
      return name;
    }
  }

  public String flairId;
  public String flairText;
  public Kind kind;
  public Boolean nsfw;
  public Boolean spoiler;
  public String sr;
  public String text;
  public String title;
  public String url;

  public SubmitOptions() {
    flairId = null;
    flairText = null;
    kind = null;
    nsfw = null;
    spoiler = null;
    sr = null;
    text = null;
    title = null;
    url = null;
  }

  public SubmitOptions setFlairId(String flairId) {
    this.flairId = flairId;
    return this;
  }

  public SubmitOptions setFlairText(String flairText) {
    this.flairText = flairText;
    return this;
  }

  public SubmitOptions setKind(Kind kind) {
    this.kind = kind;
    return this;
  }

  public SubmitOptions setNsfw(boolean nsfw) {
    this.nsfw = nsfw;
    return this;
  }

  public SubmitOptions setSpoiler(boolean spoiler) {
    this.spoiler = spoiler;
    return this;
  }

  public SubmitOptions setSubreddit(String sr) {
    this.sr = sr;
    return this;
  }

  public SubmitOptions setText(String text) {
    this.text = text;
    return this;
  }

  public SubmitOptions setTitle(String title) {
    this.title = title;
    return this;
  }

  public SubmitOptions setUrl(String url) {
    this.url = url;
    return this;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> map = new HashMap<>();
    if (flairId != null) {
      map.put("flair_id", flairId);
    }
    if (flairText != null) {
      map.put("flair_text", flairText);
    }
    if (kind != null) {
      map.put("kind", kind.getName());
    }
    if (nsfw != null) {
      map.put("nsfw", nsfw);
    }
    if (spoiler != null) {
      map.put("spoiler", spoiler);
    }
    if (sr != null) {
      map.put("sr", sr);
    }
    if (text != null) {
      map.put("text", text);
    }
    if (title != null) {
      map.put("title", title);
    }
    if (url != null) {
      map.put("url", url);
    }
    return map;
  }

}
