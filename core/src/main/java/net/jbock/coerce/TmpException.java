package net.jbock.coerce;

import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;

class TmpException extends Exception {

  final String message;

  static TmpException create(String message) {
    return new TmpException(message);
  }

  TmpException(String message) {
    this.message = message;
  }

  ValidationException asValidationException(ExecutableElement sourceMethod) {
    return ValidationException.create(sourceMethod, message);
  }

  ValidationException asValidationException(ExecutableElement sourceMethod, String newMessage) {
    return ValidationException.create(sourceMethod, newMessage);
  }
}
