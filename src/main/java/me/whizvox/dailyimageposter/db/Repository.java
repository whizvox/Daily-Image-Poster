package me.whizvox.dailyimageposter.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Repository<T> {

  protected final Connection conn;

  public Repository(Connection conn) {
    this.conn = conn;
  }

  private void prepareStatement(PreparedStatement stmt, Iterable<Object> args) throws SQLException {
    int index = 1;
    for (Object arg : args) {
      if (arg instanceof Integer i) {
        stmt.setInt(index, i);
      } else if (arg instanceof Byte b) {
        stmt.setByte(index, b);
      } else if (arg instanceof LocalDateTime ldt) {
        stmt.setTimestamp(index, Timestamp.valueOf(ldt));
      } else if (arg instanceof NullValue n) {
        stmt.setNull(index, n.sqlType());
      } else {
        stmt.setString(index, String.valueOf(arg));
      }
      index++;
    }
  }

  protected <R> R executeQuery(String sql, List<Object> args, ResultSetFunction<R> func) {
    try {
      if (args == null || args.isEmpty()) {
        try (Statement stmt = conn.createStatement()) {
          ResultSet rs = stmt.executeQuery(sql);
          return func.apply(rs);
        }
      }
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        prepareStatement(stmt, args);
        ResultSet rs = stmt.executeQuery();
        return func.apply(rs);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected <R> R executeQuery(String sql, ResultSetFunction<R> func) {
    return executeQuery(sql, null, func);
  }

  protected void execute(String sql, List<Object> args) {
    try {
      if (args == null || args.isEmpty()) {
        try (Statement stmt = conn.createStatement()) {
          stmt.execute(sql);
        }
      } else {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          prepareStatement(stmt, args);
          stmt.execute();
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void execute(String sql) {
    execute(sql, null);
  }

  protected abstract T fromRow(ResultSet rs) throws SQLException;

  protected List<T> fromRows(ResultSet rs) throws SQLException {
    List<T> list = new ArrayList<>();
    while (rs.next()) {
      list.add(fromRow(rs));
    }
    return list;
  }

  public abstract void create();

  public abstract void drop();

}