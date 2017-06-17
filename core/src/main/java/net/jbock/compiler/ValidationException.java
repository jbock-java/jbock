package net.jbock.compiler;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

final class ValidationException extends RuntimeException {
  private static final long serialVersionUID = 0;
  final Diagnostic.Kind kind;
  final Element about;

  private ValidationException(Diagnostic.Kind kind, String message, Element about) {
    super(message);
    this.kind = kind;
    this.about = about;
  }

  ValidationException(String message, Element about) {
    this(Diagnostic.Kind.ERROR, message, about);
  }
}
