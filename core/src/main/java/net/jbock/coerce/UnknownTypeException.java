package net.jbock.coerce;

import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;

class UnknownTypeException extends Exception {

  private static final String DEFAULT_MESSAGE = "Unknown parameter type. Define a custom mapper.";

  static UnknownTypeException create() {
    return new UnknownTypeException();
  }

  private UnknownTypeException() {
    super(DEFAULT_MESSAGE);
  }

  ValidationException asValidationException(ExecutableElement sourceMethod) {
    return ValidationException.create(sourceMethod, getMessage());
  }

  ValidationException asValidationException(ExecutableElement sourceMethod, String newMessage) {
    return ValidationException.create(sourceMethod, newMessage);
  }
}
