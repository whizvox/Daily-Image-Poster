package me.whizvox.dailyimageposter.reserve;

import java.time.LocalDateTime;
import java.util.UUID;

public record Reserve(UUID id,
                      String fileName,
                      String title,
                      String artist,
                      String source,
                      String comment,
                      boolean imageNsfw,
                      boolean sourceNsfw,
                      LocalDateTime whenCreated) {
}
