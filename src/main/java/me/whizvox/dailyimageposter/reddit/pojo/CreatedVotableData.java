package me.whizvox.dailyimageposter.reddit.pojo;

public class CreatedVotableData extends CreatedData implements Votable {

  public int ups;
  public int downs;
  public Boolean likes;

  @Override
  public int ups() {
    return ups;
  }

  @Override
  public int downs() {
    return downs;
  }

  @Override
  public Boolean likes() {
    return likes;
  }

}
