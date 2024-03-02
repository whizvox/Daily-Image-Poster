package me.whizvox.dailyimageposter.reddit.pojo;

public class CommentData extends CreatedVotableData {

  public String approvedBy;
  public String author;
  public String authorFlairCssClass;
  public String bannedBy;
  public String body;
  public String bodyHtml;
  /** <code>false</code> if not edited, epoch seconds if edited */
  public Object edited;
  public int gilded;
  public String linkAuthor;
  public String linkId;
  public String linkTitle;
  public Integer numReports;
  public String parentId;
  // for some reason, this is returned as an empty string???
  //public List<Comment> replies;
  public boolean saved;
  public int score;
  public boolean scoreHidden;
  public String subreddit;
  public String subredditId;
  public String distinguished;

  public boolean isEdited() {
    return edited instanceof Boolean b ? b : true;
  }

  @Override
  public String toString() {
    return "CommentData{" +
        "approvedBy='" + approvedBy + '\'' +
        ", author='" + author + '\'' +
        ", authorFlairCssClass='" + authorFlairCssClass + '\'' +
        ", bannedBy='" + bannedBy + '\'' +
        ", body='" + body + '\'' +
        ", bodyHtml='" + bodyHtml + '\'' +
        ", edited=" + edited +
        ", gilded=" + gilded +
        ", linkAuthor='" + linkAuthor + '\'' +
        ", linkId='" + linkId + '\'' +
        ", linkTitle='" + linkTitle + '\'' +
        ", numReports=" + numReports +
        ", parentId='" + parentId + '\'' +
        //", replies=" + replies +
        ", saved=" + saved +
        ", score=" + score +
        ", scoreHidden=" + scoreHidden +
        ", subreddit='" + subreddit + '\'' +
        ", subredditId='" + subredditId + '\'' +
        ", distinguished='" + distinguished + '\'' +
        ", ups=" + ups +
        ", downs=" + downs +
        ", likes=" + likes +
        ", created=" + created +
        ", createdUtc=" + createdUtc +
        '}';
  }

}
