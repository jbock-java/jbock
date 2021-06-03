package net.jbock.util;

import java.util.Locale;

/**
 * Indicates that an exception was thrown from a converter.
 */
public final class ConversionError implements Failure {

  private final Exception exception;
  private final String parameterName;
  private final ItemType itemType;

  public ConversionError(
      Exception exception,
      String parameterName,
      ItemType itemType) {
    this.exception = exception;
    this.parameterName = parameterName;
    this.itemType = itemType;
  }

  public Exception exception() {
    return exception;
  }

  public String message() {
    return "while converting " + itemType.name().toLowerCase(Locale.US) +
        " " + parameterName + ": " + exception.getMessage();
  }
}
