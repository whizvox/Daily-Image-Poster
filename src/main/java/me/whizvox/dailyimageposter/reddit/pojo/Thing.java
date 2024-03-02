package me.whizvox.dailyimageposter.reddit.pojo;

public class Thing<T> {

  public String id;
  public String name;
  public String kind;
  public T data;

  public Thing(String id, String name, String kind, T data) {
    this.id = id;
    this.name = name;
    this.kind = kind;
    this.data = data;
  }

  public Thing() {
  }

}
