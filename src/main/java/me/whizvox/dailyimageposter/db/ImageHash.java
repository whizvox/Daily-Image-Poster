package me.whizvox.dailyimageposter.db;

import dev.brachtendorf.jimagehash.hash.Hash;

public record ImageHash(String fileName, Hash hash) {
}
