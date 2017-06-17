package net.jbock.compiler;

import net.jbock.ArgumentName;
import net.jbock.Description;
import net.jbock.EverythingAfter;
import net.jbock.LongName;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.regex.Pattern;

import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;
import static net.jbock.compiler.Util.equalsType;

final class Param {

  private final Character shortName;
  final String longName;
  final OptionType optionType;
  final String parameterName;
  final String stopword;

  final Description description;
  final ArgumentName argName;

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Param(Character shortName,
                String longName,
                OptionType optionType,
                String parameterName,
                Description description,
                ArgumentName argName, String stopword) {
    this.optionType = optionType;
    this.shortName = shortName;
    this.longName = longName;
    this.parameterName = parameterName;
    this.description = description;
    this.argName = argName;
    this.stopword = stopword;
  }

  private static OptionType getOptionType(VariableElement var) {
    TypeMirror type = var.asType();
    if (type.getKind() == TypeKind.BOOLEAN) {
      return OptionType.FLAG;
    }
    if (isListOfString(type)) {
      return OptionType.REPEATABLE;
    }
    if (isOptionalString(type)) {
      return OptionType.OPTIONAL;
    }
    String message = "Only Optional<String>, List<String> and boolean allowed, " +
        String.format("but parameter %s has type %s", var.getSimpleName(), type);
    throw new ValidationException(message, var);
  }

  static Param create(VariableElement variableElement) {
    LongName longName = variableElement.getAnnotation(LongName.class);
    ShortName shortName = variableElement.getAnnotation(ShortName.class);
    OtherTokens otherTokens = variableElement.getAnnotation(OtherTokens.class);
    EverythingAfter everythingAfter = variableElement.getAnnotation(EverythingAfter.class);
    if (otherTokens != null) {
      if (everythingAfter != null) {
        throw new ValidationException(
            "@OtherTokens and @EverythingAfter cannot be on the same parameter", variableElement);
      }
      checkList(variableElement);
      return new Param(null,
          variableElement.getSimpleName().toString(),
          OptionType.OTHER_TOKENS,
          variableElement.getSimpleName().toString(),
          variableElement.getAnnotation(Description.class),
          variableElement.getAnnotation(ArgumentName.class),
          null);
    }
    if (everythingAfter != null) {
      checkList(variableElement);
      String stopword = everythingAfter.value();
      basicCheckName(variableElement, stopword);
      return new Param(null, variableElement.getSimpleName().toString(), OptionType.EVERYTHING_AFTER,
          variableElement.getSimpleName().toString(),
          variableElement.getAnnotation(Description.class),
          variableElement.getAnnotation(ArgumentName.class),
          stopword);
    }
    OptionType optionType = getOptionType(variableElement);
    String ln = null;
    Character sn = null;
    if (longName != null) {
      ln = longName.value();
    }
    if (shortName != null) {
      sn = shortName.value();
    }
    if (shortName == null && longName == null) {
      ln = variableElement.getSimpleName().toString();
    }
    if (sn != null) {
      checkName(variableElement, Character.toString(sn));
    }
    checkName(variableElement, ln);
    return new Param(sn, ln, optionType, variableElement.getSimpleName().toString(),
        variableElement.getAnnotation(Description.class),
        variableElement.getAnnotation(ArgumentName.class), null);
  }

  private static void checkList(VariableElement variableElement) {
    if (!isListOfString(variableElement.asType())) {
      throw new ValidationException("Must be a List<String>", variableElement);
    }
  }

  private static boolean isListOfString(TypeMirror type) {
    return isXOfString(type, "java.util.List");
  }

  private static boolean isOptionalString(TypeMirror type) {
    return isXOfString(type, "java.util.Optional");
  }

  private static boolean isXOfString(
      TypeMirror type, String x) {
    DeclaredType declared = type.accept(Util.AS_DECLARED, null);
    if (declared == null) {
      return false;
    }
    if (declared.getTypeArguments().size() != 1) {
      return false;
    }
    TypeElement element = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    return x.equals(
        element.getQualifiedName().toString()) &&
        equalsType(declared.getTypeArguments().get(0),
            "java.lang.String");
  }

  private static void checkName(VariableElement parameter, String name) {
    if (name == null) {
      return;
    }
    basicCheckName(parameter, name);
    if (name.startsWith("-")) {
      throw new ValidationException("The name may not start with '-'", parameter);
    }
    if (name.indexOf('=') >= 0) {
      throw new ValidationException("The name may not contain '='", parameter);
    }
  }

  private static void basicCheckName(VariableElement parameter, String name) {
    if (name == null) {
      throw new ValidationException("The name may not be null", parameter);
    }
    if (name.isEmpty()) {
      throw new ValidationException("The name may not be empty", parameter);
    }
    if (WHITE_SPACE.matcher(name).matches()) {
      throw new ValidationException("The name may not contain whitespace characters", parameter);
    }
  }

  String shortName() {
    return Objects.toString(shortName, null);
  }
}
