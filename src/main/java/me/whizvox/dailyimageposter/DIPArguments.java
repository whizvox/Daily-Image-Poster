package me.whizvox.dailyimageposter;

public final class DIPArguments {

  public String legacyDir;
  public boolean importAllLegacy;
  public boolean noReddit;
  public boolean autoRevokeRedditToken;

  public DIPArguments() {
    legacyDir = null;
    importAllLegacy = false;
    noReddit = false;
    autoRevokeRedditToken = false;
  }

}
