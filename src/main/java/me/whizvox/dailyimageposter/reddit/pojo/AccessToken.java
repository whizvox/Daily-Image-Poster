package me.whizvox.dailyimageposter.reddit.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccessToken {

  @JsonProperty("access_token")
  public String accessToken;

  @JsonProperty("token_type")
  public String tokenType;

  @JsonProperty("expires_in")
  public long expiresIn;

  public String scope;

  @JsonProperty("refresh_token")
  public String refreshToken;

  @Override
  public String toString() {
    return "AccessToken{" +
        "accessToken='" + accessToken + '\'' +
        ", tokenType='" + tokenType + '\'' +
        ", expiresIn=" + expiresIn +
        ", scope='" + scope + '\'' +
        ", refreshToken='" + refreshToken + '\'' +
        '}';
  }

}
