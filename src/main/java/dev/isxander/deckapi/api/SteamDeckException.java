package dev.isxander.deckapi.api;

public class SteamDeckException extends RuntimeException {
    public SteamDeckException(String message, Throwable th) {
        super(message, th);
    }

  public SteamDeckException(String message) {
    super(message);
  }
}
