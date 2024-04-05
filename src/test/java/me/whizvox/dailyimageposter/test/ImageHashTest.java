package me.whizvox.dailyimageposter.test;

import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import me.whizvox.dailyimageposter.image.ImageHashRepository;
import me.whizvox.dailyimageposter.util.IOHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ImageHashTest {

  static Connection conn;
  static ImageHashRepository repo;
  static PerceptiveHash hasher;

  @BeforeAll
  static void beforeAll() throws Exception {
    conn = DriverManager.getConnection("jdbc:sqlite:dip_test.db");
    repo = new ImageHashRepository(conn);
    repo.drop();
    repo.create();
    hasher = new PerceptiveHash(32);
  }

  @AfterAll
  static void afterAll() throws Exception {
    conn.close();
  }

  @BeforeEach
  void setUp() {
    repo.deleteAll();
  }

  Hash hash(String path) {
    try (InputStream in = IOHelper.getResourceStream(path)) {
      return hasher.hash(ImageIO.read(in));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  Hash addHash(String fileName) {
    Hash hash = hash(fileName);
    repo.add(fileName, hash);
    return hash;
  }

  @Test
  void checkHashIntegrity() {
    Hash hash1 = hash("bird1.jpg");
    assertEquals(hash1.normalizedHammingDistance(hash("bird1.jpg")), 0.0);
    Hash hash2 = hash("bird2.jpg");
    assertEquals(hash2.normalizedHammingDistance(hash("bird2.jpg")), 0.0);
    assertNotEquals(hash1.normalizedHammingDistance(hash2), 0.0);
    Hash hash3 = hash("bird3.jpg");
    assertEquals(hash3.normalizedHammingDistance(hash("bird3.jpg")), 0.0);
    assertNotEquals(hash1.normalizedHammingDistance(hash3), 0.0);
    assertNotEquals(hash2.normalizedHammingDistance(hash3), 0.0);
  }

  @Test
  void checkRepoIntegrity() {
    Hash hash1 = addHash("bird1.jpg");
    assertEquals(hash1, repo.get("bird1.jpg").hash());
    Hash hash2 = addHash("bird2.jpg");
    assertEquals(hash2, repo.get("bird2.jpg").hash());
    Hash hash3 = addHash("bird3.jpg");
    assertEquals(hash3, repo.get("bird3.jpg").hash());
  }

}
