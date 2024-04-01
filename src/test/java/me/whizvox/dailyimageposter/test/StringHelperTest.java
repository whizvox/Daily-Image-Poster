package me.whizvox.dailyimageposter.test;

import me.whizvox.dailyimageposter.util.StringHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringHelperTest {

  @Test
  void getRedditLinkId_valid() {
    String id = StringHelper.getRedditLinkId("https://www.reddit.com/r/u_whizvox/comments/1bsxmtf/bad_code_daily_image_3151/");
    assertEquals(id, "1bsxmtf");
  }

}
