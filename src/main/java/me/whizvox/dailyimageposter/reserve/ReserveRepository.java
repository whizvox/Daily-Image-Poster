package me.whizvox.dailyimageposter.reserve;

import me.whizvox.dailyimageposter.db.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReserveRepository extends Repository<Reserve> {

  private static final String
      SQL_CREATE =
          "CREATE TABLE IF NOT EXISTS reserves(" +
            "id CHAR(36) PRIMARY KEY, " +
            "file_name VARCHAR(255) NOT NULL UNIQUE, " +
            "title VARCHAR(255) NOT NULL, " +
            "artist VARCHAR(255) NOT NULL, " +
            "source VARCHAR(255) NOT NULL, " +
            "comment VARCHAR(1024) NOT NULL, " +
            "image_nsfw BIT NOT NULL, " +
            "source_nsfw BIT NOT NULL, " +
            "when_created TIMESTAMP NOT NULL" +
          ")",
      SQL_INSERT = "INSERT INTO reserves " +
          "(id,file_name,title,artist,source,comment,image_nsfw,source_nsfw,when_created) " +
          "VALUES (?,?,?,?,?,?,?,?,?)",
      SQL_UPDATE = "UPDATE reserves SET " +
          "file_name=?,title=?,artist=?,source=?,comment=?,image_nsfw=?,source_nsfw=? " +
          "WHERE id=?",
      SQL_SELECT_ALL = "SELECT id,file_name,title,artist,source,comment,image_nsfw,source_nsfw,when_created FROM reserves",
      SQL_SELECT_ONE = SQL_SELECT_ALL + " WHERE id=?",
      SQL_SELECT_BY_FILE = SQL_SELECT_ALL + " WHERE file_name=?",
      SQL_DELETE = "DELETE FROM reserves WHERE id=?",
      SQL_DROP = "DROP TABLE reserves";
  
  public ReserveRepository(Connection conn) {
    super(conn);
  }

  @Override
  protected Reserve _fromRow(ResultSet rs) throws SQLException {
    return new Reserve(
        UUID.fromString(rs.getString(1)),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getBoolean(7),
        rs.getBoolean(8),
        Optional.of(rs.getTimestamp(9)).map(Timestamp::toLocalDateTime).orElse(null)
    );
  }

  @Override
  public void create() {
    execute(SQL_CREATE);
  }

  @Override
  public void drop() {
    execute(SQL_DROP);
  }

  public List<Reserve> findAll() {
    return executeQuery(SQL_SELECT_ALL, this::fromRows);
  }

  public Reserve get(UUID id) {
    return executeQuery(SQL_SELECT_ONE, List.of(id), this::fromRow);
  }

  public Reserve getByFileName(String fileName) {
    return executeQuery(SQL_SELECT_BY_FILE, List.of(fileName), this::fromRow);
  }

  public void add(Reserve reserve) {
    execute(SQL_INSERT, List.of(
        reserve.id(),
        reserve.fileName(),
        reserve.title(),
        reserve.artist(),
        reserve.source(),
        reserve.comment(),
        reserve.imageNsfw(),
        reserve.sourceNsfw(),
        reserve.whenCreated()
    ));
  }

  public void update(Reserve reserve) {
    execute(SQL_UPDATE, List.of(
        reserve.fileName(),
        reserve.title(),
        reserve.artist(),
        reserve.source(),
        reserve.comment(),
        reserve.imageNsfw(),
        reserve.sourceNsfw(),
        reserve.id()
    ));
  }

  public void delete(UUID id) {
    execute(SQL_DELETE, List.of(id));
  }
  
}
