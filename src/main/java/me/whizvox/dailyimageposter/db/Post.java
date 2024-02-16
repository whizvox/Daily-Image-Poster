package me.whizvox.dailyimageposter.db;

import java.time.LocalDateTime;
import java.util.UUID;

public record Post(UUID id,
                   int number,
                   byte subNumber,
                   String title,
                   String artist,
                   String source,
                   String comment,
                   boolean postNsfw,
                   boolean sourceNsfw,
                   String redditPostId,
                   String redditCommentId,
                   String imgurId,
                   LocalDateTime whenPosted) {

  public String formatNumber() {
    if (subNumber != 0) {
      return number + "." + subNumber;
    }
    return String.valueOf(number);
  }

  @Override
  public String toString() {
    return "Post{" +
        "id=" + id +
        ", number=" + number +
        ", subNumber=" + subNumber +
        ", title='" + title + '\'' +
        ", artist='" + artist + '\'' +
        ", source='" + source + '\'' +
        ", comment='" + comment + '\'' +
        ", postNsfw=" + postNsfw +
        ", sourceNsfw=" + sourceNsfw +
        ", redditPostId='" + redditPostId + '\'' +
        ", redditCommentId='" + redditCommentId + '\'' +
        ", imgurId='" + imgurId + '\'' +
        ", whenPosted=" + whenPosted +
        '}';
  }

}
