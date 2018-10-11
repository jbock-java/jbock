package net.jbock.coerce;

import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;

class TmpException extends Exception {

  private final boolean findWarning;

  private final String message;

  static TmpException findWarning(String defaultMessage) {
    return new TmpException(true, defaultMessage);
  }

  static TmpException create(String message) {
    return new TmpException(false, message);
  }

  private TmpException(boolean findWarning, String message) {
    this.findWarning = findWarning;
    this.message = message;
  }

  ValidationException asValidationException(ExecutableElement sourceMethod) {
    return ValidationException.create(sourceMethod, message);
  }

  ValidationException asValidationException(ExecutableElement sourceMethod, String newMessage) {
    return ValidationException.create(sourceMethod, newMessage);
  }

  public boolean findWarning() {
    return findWarning;
  }
}
