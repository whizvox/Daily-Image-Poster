package me.whizvox.dailyimageposter.db;

import dev.brachtendorf.jimagehash.hash.Hash;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

public class ImageHashRepository extends Repository<ImageHash> {

  private static final String
      SQL_CREATE = "CREATE TABLE IF NOT EXISTS image_hashes(" +
        "file_name VARCHAR(255) PRIMARY KEY," +
        "hash BLOB NOT NULL," +
        "length SMALLINT NOT NULL," +
        "algorithm INT NOT NULL" +
      ")",
      SQL_DROP = "DROP TABLE IF EXISTS image_hashes",
      SQL_SELECT_ALL = "SELECT file_name,hash,length,algorithm FROM image_hashes",
      SQL_SELECT_BY_FILE = SQL_SELECT_ALL + " WHERE file_name=?",
      SQL_SELECT_BY_HASH = SQL_SELECT_ALL + " WHERE hash=?",
      SQL_INSERT = "INSERT INTO image_hashes (file_name,hash,length,algorithm) VALUES (?,?,?,?)",
      SQL_UPDATE = "UPDATE image_hashes SET hash=?,length=?,algorithm=? WHERE file_name=?",
      SQL_DELETE_ALL = "DELETE FROM image_hashes",
      SQL_DELETE_ONE = SQL_DELETE_ALL + " WHERE file_name=?";

  public ImageHashRepository(Connection conn) {
    super(conn);
  }

  @Override
  protected ImageHash _fromRow(ResultSet rs) throws SQLException {
    return new ImageHash(
        rs.getString(1),
        new Hash(
            new BigInteger(rs.getBytes(2)),
            rs.getShort(3),
            rs.getInt(4)
        )
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

  public List<ImageHash> getAll() {
    return executeQuery(SQL_SELECT_ALL, this::fromRows);
  }

  public ImageHash get(String fileName) {
    return executeQuery(SQL_SELECT_BY_FILE, List.of(fileName), this::fromRow);
  }

  public ImageHash getByHash(Hash hash) {
    return executeQuery(SQL_SELECT_BY_HASH, List.of(hash.getHashValue().toByteArray()), this::fromRow);
  }

  public void forEach(Consumer<ImageHash> consumer) {
    forEach(SQL_SELECT_ALL, consumer);
  }

  public boolean exists(String fileName) {
    return exists(SQL_SELECT_BY_FILE, List.of(fileName));
  }

  public void add(String fileName, Hash hash) {
    execute(SQL_INSERT, List.of(fileName, hash.getHashValue().toByteArray(), hash.getBitResolution(), hash.getAlgorithmId()));
  }

  public void update(String fileName, Hash hash) {
    execute(SQL_UPDATE, List.of(hash.getHashValue().toByteArray(), hash.getBitResolution(), hash.getAlgorithmId(), fileName));
  }

  public void addOrUpdate(String fileName, Hash hash) {
    if (exists(fileName)) {
      update(fileName, hash);
    } else {
      add(fileName, hash);
    }
  }

  public void delete(String fileName) {
    execute(SQL_DELETE_ONE, List.of(fileName));
  }

  public void deleteAll() {
    execute(SQL_DELETE_ALL);
  }

}
