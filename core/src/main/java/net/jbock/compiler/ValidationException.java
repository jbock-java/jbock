package net.jbock.compiler;

import javax.lang.model.element.Element;

public final class ValidationException extends RuntimeException {

  final Element about;

  private ValidationException(String message, Element about) {
    super(message);
    this.about = about;
  }

  public static ValidationException create(Element about, String message) {
    return new ValidationException(message, about);
  }
}
