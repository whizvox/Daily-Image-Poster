package me.whizvox.dailyimageposter.exception;

import java.net.http.HttpResponse;
import java.util.function.Function;

public class UnexpectedResponseException extends RuntimeException {

  public final HttpResponse<?> response;
  public final String body;

  public <BODY> UnexpectedResponseException(String message, HttpResponse<BODY> response, Function<BODY, String> body2StrFunc) {
    super(message);
    this.response = response;
    body = body2StrFunc.apply(response.body());
  }

  public <BODY> UnexpectedResponseException(HttpResponse<BODY> response, Function<BODY, String> body2StrFunc) {
    this(null, response, body2StrFunc);
  }

  public UnexpectedResponseException(String message, HttpResponse<?> response) {
    this(message, response, String::valueOf);
  }

  public UnexpectedResponseException(HttpResponse<?> response) {
    this(String.format("[%d] %s", response.statusCode(), response.body()), response);
  }

  public int statusCode() {
    return response.statusCode();
  }

  public String body() {
    return body;
  }

}
