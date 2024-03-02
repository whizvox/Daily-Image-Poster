package me.whizvox.dailyimageposter.reddit.pojo;

import java.util.List;

public class ListingData<T> {

  public class Entry {

    public String kind;
    public T data;

    public Entry(String kind, T data) {
      this.kind = kind;
      this.data = data;
    }

    public Entry() {
    }

  }

  public String after;
  public String before;
  public List<Entry> children;

  public ListingData(String after, String before, List<Entry> children) {
    this.after = after;
    this.before = before;
    this.children = children;
  }

  public ListingData() {
  }

}
