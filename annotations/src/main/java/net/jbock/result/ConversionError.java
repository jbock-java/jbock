package net.jbock.result;

/**
 * Indicates that an exception was thrown from a converter.
 */
public final class ConversionError implements NotSuccess {

  private final Exception error;

  public ConversionError(Exception error) {
    this.error = error;
  }

  public Exception getError() {
    return error;
  }
}
