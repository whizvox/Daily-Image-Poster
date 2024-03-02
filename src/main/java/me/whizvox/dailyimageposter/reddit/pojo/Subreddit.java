package me.whizvox.dailyimageposter.reddit.pojo;

public class Subreddit extends Thing<SubredditData> {

  public Subreddit(String id, String name, String kind, SubredditData data) {
    super(id, name, kind, data);
  }

  public Subreddit() {
  }

  @Override
  public String toString() {
    return "Subreddit{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", kind='" + kind + '\'' +
        ", data=" + data +
        '}';
  }

}
