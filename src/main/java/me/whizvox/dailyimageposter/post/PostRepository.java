package me.whizvox.dailyimageposter.post;

import me.whizvox.dailyimageposter.db.NullValue;
import me.whizvox.dailyimageposter.db.Repository;

import java.sql.*;
import java.util.*;

import static me.whizvox.dailyimageposter.DailyImagePoster.LOG;

public class PostRepository extends Repository<Post> {

  private static final String
      SQL_CREATE =
          "CREATE TABLE IF NOT EXISTS posts(" +
              "id CHAR(36) PRIMARY KEY, " +
              "file_name VARCHAR(255)," +
              "num INT, " +
              "sub_num TINYINT, " +
              "title VARCHAR(255), " +
              "artist VARCHAR(255), " +
              "source VARCHAR(255), " +
              "comment VARCHAR(1024), " +
              "image_nsfw BIT NOT NULL, " +
              "source_nsfw BIT NOT NULL, " +
              "reddit_post_id VARCHAR(8), " +
              "reddit_comment_id VARCHAR(8), " +
              "imgur_id VARCHAR(8), " +
              "when_posted TIMESTAMP" +
          ")",
      SQL_INSERT = "INSERT INTO posts " +
          "(id,file_name,num,sub_num,title,artist,source,comment,image_nsfw,source_nsfw,reddit_post_id,reddit_comment_id,imgur_id,when_posted) " +
          "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
      SQL_UPDATE = "UPDATE posts SET " +
          "file_name=?,num=?,sub_num=?,title=?,artist=?,source=?,comment=?,image_nsfw=?,source_nsfw=?,reddit_post_id=?,reddit_comment_id=?,imgur_id=?,when_posted=? " +
          "WHERE id=?",
      SQL_SELECT_ALL = "SELECT id,file_name,num,sub_num,title,artist,source,comment,image_nsfw,source_nsfw,reddit_post_id,reddit_comment_id,imgur_id,when_posted FROM posts",
      SQL_SELECT_ONE = SQL_SELECT_ALL + " WHERE id=?",
      SQL_SELECT_BY_FILE = SQL_SELECT_ALL + " WHERE file_name=?",
      SQL_SELECT_BY_TITLE = SQL_SELECT_ALL + " WHERE UPPER(title) LIKE '%' || ? || '%'",
      SQL_SEARCH_LAST = SQL_SELECT_ALL + " WHERE num IS NOT NULL AND sub_num IS NOT NULL ORDER BY sub_num,num DESC LIMIT 1",
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

  public Post getByFileName(String fileName) {
    return executeQuery(SQL_SELECT_BY_FILE, List.of(fileName), this::fromRow);
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

  public List<Post> search(Map<String, Object> query) {
    StringBuilder sb = new StringBuilder();
    sb.append(SQL_SELECT_ALL);
    List<Object> args = new ArrayList<>();
    if (!query.isEmpty()) {
      sb.append(" WHERE ");
      query.forEach((key, value) -> {
        if (!args.isEmpty()) {
          sb.append(" AND ");
        }
        try {
          switch (key) {
            case "fileName" -> {
              args.add(value);
              sb.append("file_name LIKE '%' || ? || '%'");
            }
            case "number" -> {
              String number = (String) value;
              int indexOf = number.indexOf('.');
              if (indexOf != -1) {
                args.add(Integer.parseInt(number.substring(0, indexOf)));
                args.add(Integer.parseInt(number.substring(indexOf + 1)));
                sb.append("num = ? AND sub_num = ?");
              } else {
                args.add(Integer.parseInt(number));
                sb.append("num = ?");
              }
            }
            case "minNumber" -> {
              args.add(Integer.parseInt((String) value));
              sb.append("num >= ?");
            }
            case "maxNumber" -> {
              args.add(Integer.parseInt((String) value));
              sb.append("num <= ?");
            }
            case "title" -> {
              args.add(value);
              sb.append("title LIKE '%' || ? || '%'");
            }
            case "artist" -> {
              args.add(value);
              sb.append("artist LIKE '%' || ? || '%'");
            }
            case "source" -> {
              args.add(value);
              sb.append("source LIKE '%' || ? || '%'");
            }
            case "comment" -> {
              args.add(value);
              sb.append("comment LIKE '%' || ? || '%'");
            }
            case "hasBeenPosted" -> {
              if ((boolean) value) {
                sb.append("when_posted IS NOT NULL");
              } else {
                sb.append("when_posted IS NULL");
              }
            }
            case "postedAfter" -> {
              args.add(value);
              sb.append("when_posted >= ?");
            }
            case "postedBefore" -> {
              args.add(value);
              sb.append("when_posted <= ?");
            }
            case "uploaded" -> {
              switch ((String) value) {
                case "imgur" -> sb.append("imgur_id IS NOT NULL");
                case "reddit" -> sb.append("imgur_id IS NULL");
                default -> throw new IllegalArgumentException();
              }
            }
          }
        } catch (IllegalArgumentException e) {
          LOG.info("Invalid value for {}: {}", key, value);
        }
      });
    }
    LOG.debug("POST SEARCH QUERY GENERATED: {}", sb);
    return executeQuery(sb.toString(), args, this::fromRows);
  }

  public void add(Post post) {
    execute(SQL_INSERT, List.of(
        post.id(),
        NullValue.of(post.fileName(), Types.VARCHAR),
        post.number(),
        post.subNumber(),
        NullValue.of(post.title(), Types.VARCHAR),
        NullValue.of(post.artist(), Types.VARCHAR),
        NullValue.of(post.source(), Types.VARCHAR),
        NullValue.of(post.comment(), Types.VARCHAR),
        post.imageNsfw(),
        post.sourceNsfw(),
        NullValue.of(post.redditPostId(), Types.VARCHAR),
        NullValue.of(post.redditCommentId(), Types.VARCHAR),
        NullValue.of(post.imgurId(), Types.VARCHAR),
        NullValue.of(post.whenPosted(), Types.TIMESTAMP)
    ));
  }

  public void update(Post post) {
    execute(SQL_UPDATE, Arrays.asList(
        NullValue.of(post.fileName(), Types.VARCHAR),
        post.number(),
        post.subNumber(),
        NullValue.of(post.title(), Types.VARCHAR),
        NullValue.of(post.artist(), Types.VARCHAR),
        NullValue.of(post.source(), Types.VARCHAR),
        NullValue.of(post.comment(), Types.VARCHAR),
        post.imageNsfw(),
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
  protected Post _fromRow(ResultSet rs) throws SQLException {
    return new Post(
        UUID.fromString(rs.getString(1)),
        rs.getString(2),
        rs.getInt(3),
        rs.getByte(4),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getString(8),
        rs.getBoolean(9),
        rs.getBoolean(10),
        rs.getString(11),
        rs.getString(12),
        rs.getString(13),
        Optional.ofNullable(rs.getTimestamp(14)).map(Timestamp::toLocalDateTime).orElse(null)
    );
  }

}
