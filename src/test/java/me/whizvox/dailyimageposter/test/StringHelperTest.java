package me.whizvox.dailyimageposter.test;

import me.whizvox.dailyimageposter.reddit.pojo.Comment;
import me.whizvox.dailyimageposter.util.StringHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StringHelperTest {

  @Test
  void getRedditLinkId_valid() {
    String id = StringHelper.getRedditLinkId("https://www.reddit.com/r/u_whizvox/comments/1bsxmtf/bad_code_daily_image_3151/");
    assertEquals(id, "1bsxmtf");
  }

  @Test
  void parseComment_valid() {
    String response = "{\"jquery\": [[0, 1, \"call\", [\"body\"]], [1, 2, \"attr\", \"find\"], [2, 3, \"call\", [\".status\"]], [3, 4, \"attr\", \"hide\"], [4, 5, \"call\", []], [5, 6, \"attr\", \"html\"], [6, 7, \"call\", [\"\"]], [7, 8, \"attr\", \"end\"], [8, 9, \"call\", []], [1, 10, \"attr\", \"find\"], [10, 11, \"call\", [\"textarea\"]], [11, 12, \"attr\", \"attr\"], [12, 13, \"call\", [\"rows\", 3]], [13, 14, \"attr\", \"html\"], [14, 15, \"call\", [\"\"]], [15, 16, \"attr\", \"val\"], [16, 17, \"call\", [\"\"]], [0, 18, \"attr\", \"insert_things\"], [18, 19, \"call\", [[{\"kind\": \"t1\", \"data\": {\"subreddit_id\": \"t5_1p5twg\", \"approved_at_utc\": null, \"author_is_blocked\": false, \"comment_type\": null, \"edited\": false, \"mod_reason_by\": null, \"banned_by\": null, \"ups\": 1, \"num_reports\": 0, \"author_flair_type\": \"text\", \"total_awards_received\": 0, \"subreddit\": \"u_whizvox\", \"author_flair_template_id\": null, \"likes\": true, \"replies\": \"\", \"user_reports\": [], \"saved\": false, \"id\": \"kxik5df\", \"banned_at_utc\": null, \"mod_reason_title\": null, \"gilded\": 0, \"archived\": false, \"collapsed_reason_code\": null, \"no_follow\": false, \"author\": \"whizvox\", \"can_mod_post\": true, \"created_utc\": 1711958358.0, \"ignore_reports\": false, \"send_replies\": true, \"parent_id\": \"t3_1bsxtv8\", \"score\": 1, \"author_fullname\": \"t2_ig6xn\", \"report_reasons\": [], \"approved_by\": null, \"all_awardings\": [], \"collapsed\": false, \"body\": \"* Artist: idk\\r\\n* Source: akjhvdjvhkas **(NSFW Warning!)**\\r\\n\\r\\n---\\r\\n\\r\\ncomment\", \"awarders\": [], \"gildings\": {}, \"author_flair_css_class\": null, \"author_patreon_flair\": false, \"downs\": 0, \"author_flair_richtext\": [], \"is_submitter\": true, \"body_html\": \"&lt;div class=\\\"md\\\"&gt;&lt;ul&gt;\\n&lt;li&gt;Artist: idk&lt;/li&gt;\\n&lt;li&gt;Source: akjhvdjvhkas &lt;strong&gt;(NSFW Warning!)&lt;/strong&gt;&lt;/li&gt;\\n&lt;/ul&gt;\\n\\n&lt;hr/&gt;\\n\\n&lt;p&gt;comment&lt;/p&gt;\\n&lt;/div&gt;\", \"removal_reason\": null, \"collapsed_reason\": null, \"spam\": false, \"associated_award\": null, \"stickied\": false, \"author_premium\": false, \"can_gild\": false, \"removed\": false, \"unrepliable_reason\": null, \"approved\": false, \"author_flair_text_color\": null, \"score_hidden\": false, \"permalink\": \"/r/u_whizvox/comments/1bsxtv8/forbidden_chord_daily_image_3151/kxik5df/\", \"subreddit_type\": \"user\", \"locked\": false, \"name\": \"t1_kxik5df\", \"created\": 1711958358.0, \"author_flair_text\": null, \"treatment_tags\": [], \"rte_mode\": \"markdown\", \"link_id\": \"t3_1bsxtv8\", \"subreddit_name_prefixed\": \"u/whizvox\", \"controversiality\": 0, \"top_awarded_type\": null, \"author_flair_background_color\": null, \"collapsed_because_crowd_control\": null, \"mod_reports\": [], \"mod_note\": null, \"distinguished\": null}}], false]], [0, 20, \"call\", [\"#noresults\"]], [20, 21, \"attr\", \"hide\"], [21, 22, \"call\", []]], \"success\": true}";
    Comment comment = StringHelper.parseCommentFromJQuery(response);
    assertNotNull(comment);
    assertEquals(comment.kind, "t1");
    assertEquals(comment.data.id, "kxik5df");
  }

}
