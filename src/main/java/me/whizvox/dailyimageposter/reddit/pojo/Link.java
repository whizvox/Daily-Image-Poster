package me.whizvox.dailyimageposter.reddit.pojo;

public class Link extends Thing<LinkData> {

  public Link(String id, String name, String kind, LinkData data) {
    super(id, name, kind, data);
  }

  public Link() {
  }

  @Override
  public String toString() {
    return "Link{" +
        "id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", kind='" + kind + '\'' +
        ", data=" + data +
        '}';
  }

}
