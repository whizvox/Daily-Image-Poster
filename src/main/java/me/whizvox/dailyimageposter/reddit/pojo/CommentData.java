package me.whizvox.dailyimageposter.reddit.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/*
All ignored fields:
allAwardings
approved
approvedAtUtc
approvedBy
archived
associatedAward
authorFlairBackgroundColor
authorFlairCssClass
authorFlairRichtext
authorFlairTemplateId
authorFlairText
authorFlairTextColor
authorFlairType
authorIsBlocked
authorPatreonFlair
authorPremium
awarders
canGild
canModPost
collapsed
collapsedBecauseCrowdControl
collapsedReason
collapsedReasonCode
commentType
controversiality
created
gilded
gildings
ignoreReports
locked
modNote
modReasonBy
modReasonTitle
modReports
noFollow
numReports
removalReason
removed
replies
reportReasons
rteMode
scoreHidden
sendReplies
spam
stickied
subredditType
topAwardedType
totalAwardsReceived
treatmentTags
unrepliableReason
userReports

Present in the documentation but not in actual responses:
linkTitle
linkUrl

Also, the documentation is flat-out wrong about the replies field. It's always an empty string no matter what. Maybe it
used to contain meaningful data but no longer does?
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentData extends CreatedVotableData {

  public String id;
  /** Username of author */
  public String author;
  public String authorFullname;
  public Double bannedAtUtc;
  public String bannedBy;
  public String body;
  public String bodyHtml;
  public Double createdUtc;
  /** One of several values: null, "moderator", "admin", or "special" */
  public String distinguished;
  public Object edited;
  public boolean isSubmitter;
  public String linkId;
  /** Fullname of this comment */
  public String name;
  public String parentId;
  /** Portion of URL to this comment, does not include the hostname "https://reddit.com" and starts with "/r/" */
  public String permalink;
  public boolean saved;
  public String subreddit;
  /** Fullname of subreddit */
  public String subredditId;
  /** Same as subreddit field, but prefixed with "r/" */
  public String subredditNamePrefixed;

  public boolean isEdited() {
    return edited instanceof Boolean b ? b : true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CommentData that = (CommentData) o;
    return isSubmitter == that.isSubmitter && saved == that.saved && Objects.equals(id, that.id) &&
        Objects.equals(author, that.author) && Objects.equals(authorFullname, that.authorFullname) &&
        Objects.equals(bannedAtUtc, that.bannedAtUtc) && Objects.equals(bannedBy, that.bannedBy) &&
        Objects.equals(body, that.body) && Objects.equals(bodyHtml, that.bodyHtml) &&
        Objects.equals(createdUtc, that.createdUtc) && Objects.equals(distinguished, that.distinguished) &&
        Objects.equals(edited, that.edited) && Objects.equals(linkId, that.linkId) && Objects.equals(name, that.name) &&
        Objects.equals(parentId, that.parentId) && Objects.equals(permalink, that.permalink) &&
        Objects.equals(subreddit, that.subreddit) && Objects.equals(subredditId, that.subredditId) &&
        Objects.equals(subredditNamePrefixed, that.subredditNamePrefixed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, author, authorFullname, bannedAtUtc, bannedBy, body, bodyHtml, createdUtc, distinguished,
        edited, isSubmitter, linkId, name, parentId, permalink, saved, subreddit, subredditId, subredditNamePrefixed);
  }

  @Override
  public String toString() {
    return "CommentData{" +
        "id='" + id + '\'' +
        ", author='" + author + '\'' +
        ", authorFullname='" + authorFullname + '\'' +
        ", bannedAtUtc=" + bannedAtUtc +
        ", bannedBy='" + bannedBy + '\'' +
        ", body='" + body + '\'' +
        ", bodyHtml='" + bodyHtml + '\'' +
        ", createdUtc=" + createdUtc +
        ", distinguished='" + distinguished + '\'' +
        ", edited=" + edited +
        ", isSubmitter=" + isSubmitter +
        ", linkId='" + linkId + '\'' +
        ", name='" + name + '\'' +
        ", parentId='" + parentId + '\'' +
        ", permalink='" + permalink + '\'' +
        ", saved=" + saved +
        ", subreddit='" + subreddit + '\'' +
        ", subredditId='" + subredditId + '\'' +
        ", subredditNamePrefixed='" + subredditNamePrefixed + '\'' +
        '}';
  }

}
