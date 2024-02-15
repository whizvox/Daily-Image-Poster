package me.whizvox.dailyimageposter.db;

import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostRepository extends Repository<Post> {

  private static final String
      SQL_CREATE =
          "CREATE TABLE IF NOT EXISTS posts(" +
              "id CHAR(36) PRIMARY KEY, " +
              "num INT, " +
              "sub_num TINYINT, " +
              "title VARCHAR(255), " +
              "artist VARCHAR(255), " +
              "source VARCHAR(255), " +
              "comment VARCHAR(1024), " +
              "post_nsfw BIT NOT NULL, " +
              "source_nsfw BIT NOT NULL, " +
              "reddit_post_id VARCHAR(8), " +
              "reddit_comment_id VARCHAR(8), " +
              "imgur_id VARCHAR(8), " +
              "when_posted TIMESTAMP" +
          ")",
      SQL_INSERT = "INSERT INTO posts " +
          "(id,num,sub_num,title,artist,source,comment,post_nsfw,source_nsfw,reddit_post_id,reddit_comment_id,imgur_id,when_posted) " +
          "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
      SQL_UPDATE = "UPDATE posts SET " +
          "num=?,sub_num=?,title=?,artist=?,source=?,comment=?,post_nsfw=?,source_nsfw=?,reddit_post_id=?,reddit_comment_id=?,imgur_id=?,when_posted=? " +
          "WHERE id=?",
      SQL_SELECT_ALL = "SELECT id,num,sub_num,title,artist,source,comment,post_nsfw,source_nsfw,reddit_post_id,reddit_comment_id,imgur_id,when_posted FROM posts",
      SQL_SELECT_ONE = SQL_SELECT_ALL + " WHERE id=?",
      SQL_SELECT_BY_TITLE = SQL_SELECT_ALL + " WHERE UPPER(title) LIKE '%' || ? || '%'",
      SQL_SEARCH_LAST = SQL_SELECT_ONE + " WHERE num IS NOT NULL AND sub_num IS NOT NULL ORDER BY num,sub_num LIMIT 1",
      SQL_SELECT_ALL_POSTED = SQL_SELECT_ALL + " WHERE when_posted IS NOT NULL",
      SQL_DELETE_ALL = "DELETE FROM posts",
      SQL_DELETE_ONE = SQL_DELETE_ALL + " WHERE id=?",
      SQL_DROP = "DROP TABLE posts";

  public PostRepository(Connection conn) {
    super(conn);
  }

  @Override
  public void create() {
    execute(SQL_CREATE);
  }

  @Override
  public void drop() {
    execute(SQL_DROP);
  }

  public Post get(UUID id) {
    return executeQuery(SQL_SELECT_ONE, List.of(id), this::fromRow);
  }

  public List<Post> getAll() {
    return executeQuery(SQL_SELECT_ALL, this::fromRows);
  }

  public List<Post> searchTitle(String query) {
    return executeQuery(SQL_SELECT_BY_TITLE, List.of(query), this::fromRows);
  }

  public Post getLast() {
    return executeQuery(SQL_SEARCH_LAST, this::fromRow);
  }

  public List<Post> getAllPosted() {
    return executeQuery(SQL_SELECT_ALL_POSTED, this::fromRows);
  }

  public void add(Post post) {
    execute(SQL_INSERT, List.of(
        post.id(),
        post.number(),
        post.subNumber(),
        NullValue.of(post.title(), Types.VARCHAR),
        NullValue.of(post.artist(), Types.VARCHAR),
        NullValue.of(post.source(), Types.VARCHAR),
        NullValue.of(post.comment(), Types.VARCHAR),
        post.postNsfw(),
        post.sourceNsfw(),
        NullValue.of(post.redditPostId(), Types.VARCHAR),
        NullValue.of(post.redditCommentId(), Types.VARCHAR),
        NullValue.of(post.imgurId(), Types.VARCHAR),
        NullValue.of(post.whenPosted(), Types.TIMESTAMP)
    ));
  }

  public void update(Post post) {
    execute(SQL_UPDATE, Arrays.asList(
        post.number(),
        post.subNumber(),
        NullValue.of(post.title(), Types.VARCHAR),
        NullValue.of(post.artist(), Types.VARCHAR),
        NullValue.of(post.source(), Types.VARCHAR),
        NullValue.of(post.comment(), Types.VARCHAR),
        post.postNsfw(),
        post.sourceNsfw(),
        NullValue.of(post.redditPostId(), Types.VARCHAR),
        NullValue.of(post.redditCommentId(), Types.VARCHAR),
        NullValue.of(post.imgurId(), Types.VARCHAR),
        NullValue.of(post.whenPosted(), Types.TIMESTAMP),
        post.id()
    ));
  }

  public void delete(UUID postId) {
    execute(SQL_DELETE_ONE, List.of(postId));
  }

  public void deleteAll() {
    execute(SQL_DELETE_ALL);
  }

  @Override
  protected Post fromRow(ResultSet rs) throws SQLException {
    return new Post(
        UUID.fromString(rs.getString(1)),
        rs.getInt(2),
        rs.getByte(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getBoolean(8),
        rs.getBoolean(9),
        rs.getString(10),
        rs.getString(11),
        rs.getString(12),
        Optional.ofNullable(rs.getTimestamp(13)).map(Timestamp::toLocalDateTime).orElse(null)
    );
  }

}