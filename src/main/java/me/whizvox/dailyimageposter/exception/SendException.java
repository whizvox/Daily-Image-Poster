package me.whizvox.dailyimageposter.exception;

public class SendException extends RuntimeException {

  public SendException() {
  }

  public SendException(String message) {
    super(message);
  }

  public SendException(String message, Throwable cause) {
    super(message, cause);
  }

}
