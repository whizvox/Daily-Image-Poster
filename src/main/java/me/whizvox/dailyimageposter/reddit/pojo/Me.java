package me.whizvox.dailyimageposter.reddit.pojo;

import java.time.LocalDateTime;

public class Me {

  public int commentKarma;
  public LocalDateTime created;
  public long createdUtc;
  public boolean hasMail;
  public boolean hasModMail;
  public boolean hasVerifiedEmail;
  public String id;
  public boolean isGold;
  public boolean isMod;
  public int linkKarma;
  public String name;
  public boolean over18;

  @Override
  public String toString() {
    return "Me{" +
        "commentKarma=" + commentKarma +
        ", created=" + created +
        ", createdUtc=" + createdUtc +
        ", hasMail=" + hasMail +
        ", hasModMail=" + hasModMail +
        ", hasVerifiedEmail=" + hasVerifiedEmail +
        ", id='" + id + '\'' +
        ", isGold=" + isGold +
        ", isMod=" + isMod +
        ", linkKarma=" + linkKarma +
        ", name='" + name + '\'' +
        ", over18=" + over18 +
        '}';
  }

}
