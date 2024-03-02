package me.whizvox.dailyimageposter.reddit.pojo;

public class Comment extends Thing<CommentData> {

  public Comment(String id, String name, String kind, CommentData data) {
    super(id, name, kind, data);
  }

  public Comment() {
  }

  @Override
  public String toString() {
    return "Comment{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", kind='" + kind + '\'' +
        ", data=" + data +
        '}';
  }

}
