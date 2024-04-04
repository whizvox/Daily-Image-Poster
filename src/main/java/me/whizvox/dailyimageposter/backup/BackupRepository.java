package me.whizvox.dailyimageposter.backup;

import me.whizvox.dailyimageposter.db.Repository;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class BackupRepository extends Repository<Backup> {

  private static final String
      SQL_CREATE = "CREATE TABLE IF NOT EXISTS backups(" +
          "file_name VARCHAR(255) PRIMARY KEY," +
          "orig_file_name VARCHAR(255) NOT NULL," +
          "sha1 CHAR(40) NOT NULL," +
          "created TIMESTAMP NOT NULL" +
      ")",
      SQL_DROP = "DROP TABLE backups",
      SQL_SELECT_ALL = "SELECT file_name,orig_file_name,sha1,created FROM backups",
      SQL_SELECT_FILE = SQL_SELECT_ALL + " WHERE file_name=?",
      SQL_SELECT_ORIG_FILE = SQL_SELECT_ALL + " WHERE orig_file_name=?",
      SQL_SELECT_HASH = SQL_SELECT_ALL + " WHERE sha1=?",
      SQL_SELECT_OLDER = SQL_SELECT_ALL + " WHERE created<?",
      SQL_INSERT = "INSERT INTO backups (file_name,orig_file_name,sha1,created) VALUES (?,?,?,?)",
      SQL_UPDATE_HASH = "UPDATE backups SET sha1=? WHERE file_name=?",
      SQL_DELETE = "DELETE FROM backups WHERE file_name=?",
      SQL_DELETE_ALL_ORIG = "DELETE FROM backups WHERE orig_file_name=?";

  public BackupRepository(Connection conn) {
    super(conn);
  }

  @Override
  protected Backup _fromRow(ResultSet rs) throws SQLException {
    return new Backup(rs.getString(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4).toLocalDateTime());
  }

  @Override
  public void create() {
    execute(SQL_CREATE);
  }

  @Override
  public void drop() {
    execute(SQL_DROP);
  }

  public boolean isValidSha1Hash(String sha1) {
    return sha1 != null && sha1.length() == 40;
  }

  public void checkSha1(String sha1) {
    if (!isValidSha1Hash(sha1)) {
      throw new IllegalArgumentException("Invalid SHA1 hash, must be 40 chars long: " + sha1);
    }
  }

  public boolean exists(String fileName) {
    return exists(SQL_SELECT_FILE, List.of(fileName));
  }

  public void forEach(Consumer<Backup> consumer) {
    forEach(SQL_SELECT_ALL, consumer);
  }

  public List<Backup> getAll() {
    return executeQuery(SQL_SELECT_ALL, this::fromRows);
  }

  public List<Backup> getAllOlderThan(LocalDateTime ldt) {
    return executeQuery(SQL_SELECT_OLDER, List.of(ldt), this::fromRows);
  }

  @Nullable
  public Backup get(String fileName) {
    return executeQuery(SQL_SELECT_FILE, List.of(fileName), this::fromRow);
  }

  @Nullable
  public Backup getByHash(String sha1) {
    return executeQuery(SQL_SELECT_HASH, List.of(sha1), this::fromRow);
  }

  public List<Backup> getAllOf(String origFileName) {
    return executeQuery(SQL_SELECT_ORIG_FILE, List.of(origFileName), this::fromRows);
  }

  public void add(String fileName, String origFileName, String sha1, LocalDateTime created) {
    checkSha1(sha1);
    execute(SQL_INSERT, List.of(fileName, origFileName, sha1, created));
  }

  public void updateHash(String fileName, String sha1) {
    checkSha1(sha1);
    execute(SQL_UPDATE_HASH, List.of(sha1, fileName));
  }

  public void delete(String fileName) {
    execute(SQL_DELETE, List.of(fileName));
  }

  public void deleteAllOf(String origFileName) {
    execute(SQL_DELETE_ALL_ORIG, List.of(origFileName));
  }

}
