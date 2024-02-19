package me.whizvox.dailyimageposter.reddit;

import java.util.List;
import java.util.Map;

public class UploadMediaLease {

  public static class FieldEntry {
    public String name;
    public String value;
  }

  public static class Arguments {
    public String action;
    public List<FieldEntry> fields;
  }

  public static class Asset {
    public String assetId;
    public String processingState;
    public Map<String, String> payload;
    public String websocketUrl;
  }

  public Arguments args;
  public Asset asset;

}
