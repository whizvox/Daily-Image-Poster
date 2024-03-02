package me.whizvox.dailyimageposter.reddit.pojo;

public class Listing<T> {

  public String kind;
  public ListingData<T> data;

  public Listing(String kind, ListingData<T> data) {
    this.kind = kind;
    this.data = data;
  }

  public Listing() {
  }

  @Override
  public String toString() {
    return "Listing{" +
        "kind='" + kind + '\'' +
        ", data=" + data +
        '}';
  }

}
