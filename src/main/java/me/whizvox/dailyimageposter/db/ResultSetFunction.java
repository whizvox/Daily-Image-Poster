package me.whizvox.dailyimageposter.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetFunction<T> {

  T apply(ResultSet rs) throws SQLException;

}
