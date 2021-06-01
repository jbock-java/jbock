package net.jbock;

public class ParsingFailed extends NoResult {

  private final Exception error;

  private ParsingFailed(Exception error) {
    this.error = error;
  }

  Exception getError() {
    return error;
  }
}
