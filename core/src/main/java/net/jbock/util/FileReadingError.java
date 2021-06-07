package net.jbock.util;

import net.jbock.model.CommandModel;

/**
 * An instance of this class signals failure of a file reading operation.
 * This is an intermediate step in the construction of an
 * {@link AtFileError} instance.
 */
public final class FileReadingError {

  private final Exception exception;
  private final String file;

  FileReadingError(Exception exception, String file) {
    this.exception = exception;
    this.file = file;
  }

  /**
   * Add the command model.
   * Public method that may be invoked from the generated code.
   *
   * @param model command model
   * @return a failure object
   */
  public NotSuccess addModel(CommandModel model) {
    return new AtFileError(model, file, exception);
  }
}
