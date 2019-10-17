package net.jbock.coerce;

public class MapperFailure {

  private final String message;

  MapperFailure(String message) {
    this.message = message;
  }

  public String getMessage() {
    return String.format("There is a problem with the mapper class: %s.", message);
  }
}
