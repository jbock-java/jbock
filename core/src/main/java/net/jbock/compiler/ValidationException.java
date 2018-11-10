package net.jbock.compiler;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

public final class ValidationException extends RuntimeException {

  private static final long serialVersionUID = 0;

  private static final List<Class<?>> KNOWN_CLASSES = Arrays.asList(
      List.class,
      Set.class,
      Map.class,
      Optional.class,
      OptionalInt.class,
      OptionalLong.class,
      OptionalDouble.class,
      Collector.class,
      Function.class);

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
    message = message.replace("java.lang.", "");
    for (Class<?> knownClass : KNOWN_CLASSES) {
      message = message.replace(knownClass.getCanonicalName(), knownClass.getSimpleName());
    }
    return message;
  }
}
