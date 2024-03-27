package me.whizvox.dailyimageposter.db;

import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
      } else if (arg instanceof byte[] b) {
        stmt.setBytes(index, b);
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

  protected void forEach(String sql, List<Object> args, Consumer<T> consumer) {
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      prepareStatement(stmt, args);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        T row = fromRow0(rs);
        consumer.accept(row);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  protected void forEach(String sql, Consumer<T> consumer) {
    forEach(sql, List.of(), consumer);
  }

  protected abstract T fromRow0(ResultSet rs) throws SQLException;

  @Nullable
  protected T fromRow(ResultSet rs) throws SQLException {
    if (rs.next()) {
      return fromRow0(rs);
    }
    return null;
  }

  protected List<T> fromRows(ResultSet rs) throws SQLException {
    List<T> list = new ArrayList<>();
    while (rs.next()) {
      list.add(fromRow0(rs));
    }
    return list;
  }

  public abstract void create();

  public abstract void drop();

}
