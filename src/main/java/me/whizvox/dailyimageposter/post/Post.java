package me.whizvox.dailyimageposter.post;

import java.time.LocalDateTime;
import java.util.UUID;

public record Post(UUID id,
                   String fileName,
                   int number,
                   byte subNumber,
                   String title,
                   String artist,
                   String source,
                   String comment,
                   boolean imageNsfw,
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
        ", fileName='" + fileName + '\'' +
        ", number=" + number +
        ", subNumber=" + subNumber +
        ", title='" + title + '\'' +
        ", artist='" + artist + '\'' +
        ", source='" + source + '\'' +
        ", comment='" + comment + '\'' +
        ", imageNsfw=" + imageNsfw +
        ", sourceNsfw=" + sourceNsfw +
        ", redditPostId='" + redditPostId + '\'' +
        ", redditCommentId='" + redditCommentId + '\'' +
        ", imgurId='" + imgurId + '\'' +
        ", whenPosted=" + whenPosted +
        '}';
  }

}
