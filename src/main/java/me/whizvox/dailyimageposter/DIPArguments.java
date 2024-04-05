package me.whizvox.dailyimageposter;

public final class DIPArguments {

  public boolean noReddit;
  public boolean autoRevokeRedditToken;

  public DIPArguments() {
    noReddit = false;
    autoRevokeRedditToken = false;
  }

  public void parse(String[] args) {
    for (String arg : args) {
      if (arg.equals("--noreddit")) {
        noReddit = true;
      } else if (arg.equals("--autorevokereddit")) {
        autoRevokeRedditToken = true;
      }
    }
  }

}
