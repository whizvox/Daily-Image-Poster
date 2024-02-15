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

}
