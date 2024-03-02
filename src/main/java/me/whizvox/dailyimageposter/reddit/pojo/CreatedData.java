package me.whizvox.dailyimageposter.reddit.pojo;

public class CreatedData implements Created {

  public long created;
  public long createdUtc;

  @Override
  public long created() {
    return created;
  }

  @Override
  public long createdUtc() {
    return createdUtc;
  }

}
