package me.whizvox.dailyimageposter.image;

import dev.brachtendorf.jimagehash.hash.Hash;

public record ImageHash(String fileName, Hash hash) {
}
