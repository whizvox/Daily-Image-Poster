package me.whizvox.dailyimageposter.reddit.pojo;

import java.util.Objects;

public class LinkFlair {

  public String type;
  public boolean textEditable;
  public String allowableContent;
  public String text;
  public int maxEmojis;
  public String textColor;
  public boolean modOnly;
  public String cssClass;
  public Object richtext;
  public String backgroundColor;
  public String id;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LinkFlair linkFlair = (LinkFlair) o;
    return textEditable == linkFlair.textEditable && maxEmojis == linkFlair.maxEmojis && modOnly == linkFlair.modOnly &&
        Objects.equals(type, linkFlair.type) && Objects.equals(allowableContent, linkFlair.allowableContent) &&
        Objects.equals(text, linkFlair.text) && Objects.equals(textColor, linkFlair.textColor) &&
        Objects.equals(cssClass, linkFlair.cssClass) && Objects.equals(richtext, linkFlair.richtext) &&
        Objects.equals(backgroundColor, linkFlair.backgroundColor) && Objects.equals(id, linkFlair.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, textEditable, allowableContent, text, maxEmojis, textColor, modOnly, cssClass, richtext,
        backgroundColor, id);
  }

  @Override
  public String toString() {
    return "LinkFlair{" +
        "type='" + type + '\'' +
        ", textEditable=" + textEditable +
        ", allowableContent='" + allowableContent + '\'' +
        ", text='" + text + '\'' +
        ", maxEmojis=" + maxEmojis +
        ", textColor='" + textColor + '\'' +
        ", modOnly=" + modOnly +
        ", cssClass='" + cssClass + '\'' +
        ", richtext=" + richtext +
        ", backgroundColor='" + backgroundColor + '\'' +
        ", id='" + id + '\'' +
        '}';
  }

}
