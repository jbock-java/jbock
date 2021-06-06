package net.jbock.util;

import net.jbock.model.CommandModel;

public final class FileReadingError {

  private final Exception exception;
  private final String file;

  FileReadingError(Exception exception, String file) {
    this.exception = exception;
    this.file = file;
  }

  public NotSuccess addModel(CommandModel model) {
    return new AtFileError(model, file, exception);
  }
}
