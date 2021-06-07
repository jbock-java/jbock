package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * Indicates an error that was thrown while reading options from
 * the {@code @file}.
 */
public final class AtFileError extends NotSuccess implements HasMessage {

  private final Exception exception;
  private final String atFile;

  /**
   * Public constructor that may be invoked from the generated code.
   *
   * @param exception exception that was thrown while reading the at file
   * @param atFile path of the at file
   */
  public AtFileError(
      CommandModel commandModel,
      String atFile,
      Exception exception) {
    super(commandModel);
    this.exception = exception;
    this.atFile = atFile;
  }

  /**
   * Returns the exception.
   *
   * @return the exception
   */
  public Exception exception() {
    return exception;
  }

  @Override
  public String message() {
    return "while reading " + atFile + ": " + exception.getMessage();
  }
}
