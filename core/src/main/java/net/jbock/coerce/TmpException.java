package net.jbock.coerce;

import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;

class TmpException extends Exception {

  static TmpException create(String message) {
    return new TmpException(message);
  }

  private TmpException(String message) {
    super(message);
  }

  ValidationException asValidationException(ExecutableElement sourceMethod) {
    return ValidationException.create(sourceMethod, getMessage());
  }
}
