package me.whizvox.dailyimageposter.reddit.pojo;

public class LinkData extends CreatedVotableData {

  public String author;
  public String authorFlairCssClass;
  public String authorFlairText;
  public boolean clicked;
  public String domain;
  public boolean hidden;
  public boolean isSelf;
  public String linkFlairCssClass;
  public String linkFlairText;
  public boolean locked;
  // Object media, Object mediaEmbed;
  public int numComments;
  public boolean over18;
  public String permalink;
  public boolean saved;
  public int score;
  public String selftext;
  public String selftextHtml;
  public String subreddit;
  public String subredditId;
  public String thumbnail;
  public String title;
  public String url;
  public long edited;
  public String distinguished;
  public boolean stickied;

  @Override
  public String toString() {
    return "LinkData{" +
        "author='" + author + '\'' +
        ", authorFlairCssClass='" + authorFlairCssClass + '\'' +
        ", authorFlairText='" + authorFlairText + '\'' +
        ", clicked=" + clicked +
        ", domain='" + domain + '\'' +
        ", hidden=" + hidden +
        ", isSelf=" + isSelf +
        ", linkFlairCssClass='" + linkFlairCssClass + '\'' +
        ", linkFlairText='" + linkFlairText + '\'' +
        ", locked=" + locked +
        ", numComments=" + numComments +
        ", over18=" + over18 +
        ", permalink='" + permalink + '\'' +
        ", saved=" + saved +
        ", score=" + score +
        ", selftext='" + selftext + '\'' +
        ", selftextHtml='" + selftextHtml + '\'' +
        ", subreddit='" + subreddit + '\'' +
        ", subredditId='" + subredditId + '\'' +
        ", thumbnail='" + thumbnail + '\'' +
        ", title='" + title + '\'' +
        ", url='" + url + '\'' +
        ", edited=" + edited +
        ", distinguished='" + distinguished + '\'' +
        ", stickied=" + stickied +
        ", ups=" + ups +
        ", downs=" + downs +
        ", likes=" + likes +
        ", created=" + created +
        ", createdUtc=" + createdUtc +
        '}';
  }

}
