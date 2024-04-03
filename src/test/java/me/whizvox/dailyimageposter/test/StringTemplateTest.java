package me.whizvox.dailyimageposter.test;

import org.junit.jupiter.api.Test;
import org.stringtemplate.v4.ST;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringTemplateTest {

  @Test
  void commentTemplateTest() {
    String templateStr = "* Artist: <artist>\n* Source: <source><if(sourceNsfw)> **(NSFW Warning!)**<endif><if(comment)>\n\n---\n\n<comment><endif>";
    ST st = new ST(templateStr);
    st.add("artist", "ARTIST");
    st.add("source", "SOURCE");
    st.add("sourceNsfw", true);
    st.add("comment", "COMMENT");
    // WHY ADD A CARRIAGE RETURN?????????????????????????
    assertEquals(st.render().replaceAll("\r", ""), "* Artist: ARTIST\n* Source: SOURCE **(NSFW Warning!)**\n\n---\n\nCOMMENT");

    st.remove("comment");
    st.add("comment", null);
    assertEquals(st.render().replaceAll("\r", ""), "* Artist: ARTIST\n* Source: SOURCE **(NSFW Warning!)**");
  }

  @Test
  void titleTemplateTest() {
    String templateStr = "<title> (Daily Images #<number>)";
    ST st = new ST(templateStr);
    st.add("title", "TITLE");
    st.add("number", "1000");
    assertEquals(st.render(), "TITLE (Daily Images #1000)");
  }

}
