package net.jbock.compiler;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public final class ValidationException extends RuntimeException {

  private static final long serialVersionUID = 0;
  final Diagnostic.Kind kind;
  final Element about;

  private ValidationException(Diagnostic.Kind kind, String message, Element about) {
    super(message);
    this.kind = kind;
    this.about = about;
  }

  public static ValidationException create(Element about, String message) {
    return new ValidationException(Diagnostic.Kind.ERROR, cleanMessage(message), about);
  }

  private static String cleanMessage(String message) {
    if (!message.contains("java.")) {
      return message;
    }
    message = message.replace("java.lang.String", "String");
    message = message.replace("java.util.List", "List");
    message = message.replace("java.util.Optional", "Optional");
    message = message.replace("java.util.OptionalInt", "OptionalInt");
    return message;
  }
}
