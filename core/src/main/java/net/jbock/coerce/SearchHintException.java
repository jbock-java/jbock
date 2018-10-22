package net.jbock.coerce;

import net.jbock.compiler.ValidationException;

import javax.lang.model.element.ExecutableElement;

class SearchHintException extends Exception {

  static SearchHintException create(String message) {
    return new SearchHintException(message);
  }

  private SearchHintException(String message) {
    super(message);
  }

  ValidationException asValidationException(ExecutableElement sourceMethod) {
    return ValidationException.create(sourceMethod, getMessage());
  }

  ValidationException asValidationException(ExecutableElement sourceMethod, String newMessage) {
    return ValidationException.create(sourceMethod, newMessage);
  }
}
