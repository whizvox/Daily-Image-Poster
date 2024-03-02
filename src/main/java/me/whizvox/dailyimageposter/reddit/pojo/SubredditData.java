package me.whizvox.dailyimageposter.reddit.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class SubredditData {

  public int accountsActive;
  public int commentScoreHideMins;
  public String description;
  public String descriptionHtml;
  public String displayName;
  public String headerImg;
  public int[] headerSize;
  public String headerTitle;
  @JsonProperty("over18")
  public boolean over18;
  public String publicDescription;
  public boolean publicTraffic;
  public long subscribers;
  public String submissionType;
  public String submitLinkLabel;
  public String submitTextLabel;
  public String subredditType;
  public String title;
  public String url;
  public boolean userIsBanned;
  public boolean userIsContributor;
  public boolean userIsModerator;
  public boolean userIsSubscriber;

  @Override
  public String toString() {
    return "SubredditData{" +
        "accountsActive=" + accountsActive +
        ", commentScoreHideMins=" + commentScoreHideMins +
        ", description='" + description + '\'' +
        ", descriptionHtml='" + descriptionHtml + '\'' +
        ", displayName='" + displayName + '\'' +
        ", headerImg='" + headerImg + '\'' +
        ", headerSize=" + Arrays.toString(headerSize) +
        ", headerTitle='" + headerTitle + '\'' +
        ", over18=" + over18 +
        ", publicDescription='" + publicDescription + '\'' +
        ", publicTraffic=" + publicTraffic +
        ", subscribers=" + subscribers +
        ", submissionType='" + submissionType + '\'' +
        ", submitLinkLabel='" + submitLinkLabel + '\'' +
        ", submitTextLabel='" + submitTextLabel + '\'' +
        ", subredditType='" + subredditType + '\'' +
        ", title='" + title + '\'' +
        ", url='" + url + '\'' +
        ", userIsBanned=" + userIsBanned +
        ", userIsContributor=" + userIsContributor +
        ", userIsModerator=" + userIsModerator +
        ", userIsSubscriber=" + userIsSubscriber +
        '}';
  }

}
