package net.jbock.util;

/**
 * Indicates that an exception was thrown from a converter.
 */
public final class ConverterThrewException implements ConverterFailure {

  private final Exception exception;

  ConverterThrewException(Exception exception) {
    this.exception = exception;
  }

  @Override
  public String message() {
    return exception.getMessage();
  }

  /**
   * Returns the exception that was caught in {@link StringConverter#apply(String)}
   *
   * @return the exception
   */
  public Exception exception() {
    return exception;
  }
}
