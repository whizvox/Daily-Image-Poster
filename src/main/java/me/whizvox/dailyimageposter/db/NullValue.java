package me.whizvox.dailyimageposter.db;

public record NullValue(int sqlType) {

  public static Object of(Object obj, int sqlType) {
    if (obj == null) {
      return new NullValue(sqlType);
    }
    return obj;
  }

}
