package net.jbock.compiler;

import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;
import static net.jbock.compiler.Util.equalsType;

import java.util.Objects;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import net.jbock.ArgumentName;
import net.jbock.Description;
import net.jbock.EverythingAfter;
import net.jbock.LongName;
import net.jbock.OtherTokens;
import net.jbock.ShortName;

final class Param {

  private final String longName;
  private final Character shortName;
  private final OptionType optionType;

  private final String stopword;

  final VariableElement variableElement;
  final ExecutableElement executableElement;

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Param(
      Character shortName,
      String longName,
      String stopword,
      VariableElement variableElement,
      ExecutableElement executableElement) {
    this.shortName = shortName;
    this.longName = longName;
    this.stopword = stopword;
    this.variableElement = variableElement;
    this.optionType = getOptionType(variableElement);
    this.executableElement = executableElement;
  }

  private static OptionType getOptionType(VariableElement variableElement) {
    if (variableElement.getAnnotation(OtherTokens.class) != null) {
      return OptionType.OTHER_TOKENS;
    }
    if (variableElement.getAnnotation(EverythingAfter.class) != null) {
      return OptionType.EVERYTHING_AFTER;
    }
    TypeMirror type = variableElement.asType();
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
        String.format("but parameter %s has type %s", variableElement.getSimpleName(), type);
    throw new ValidationException(message, variableElement);
  }

  static Param create(ExecutableElement executableElement, VariableElement variableElement) {
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
      if (longName != null) {
        throw new ValidationException(
            "@OtherTokens and @LongName cannot be on the same parameter", variableElement);
      }
      if (shortName != null) {
        throw new ValidationException(
            "@OtherTokens and @ShortName cannot be on the same parameter", variableElement);
      }
      return new Param(null,
          variableElement.getSimpleName().toString(),
          null,
          variableElement,
          executableElement);
    }
    if (everythingAfter != null) {
      checkList(variableElement);
      if (longName != null) {
        throw new ValidationException(
            "@EverythingAfter and @LongName cannot be on the same parameter", variableElement);
      }
      if (shortName != null) {
        throw new ValidationException(
            "@EverythingAfter and @ShortName cannot be on the same parameter", variableElement);
      }
      String stopword = everythingAfter.value();
      basicCheckName(variableElement, stopword);
      return new Param(null,
          variableElement.getSimpleName().toString(),
          stopword,
          variableElement, executableElement);
    }
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
    return new Param(
        sn,
        ln,
        null,
        variableElement, executableElement);
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

  String longName() {
    return longName;
  }

  public String stopword() {
    return stopword;
  }

  Description description() {
    return variableElement.getAnnotation(Description.class);
  }

  ArgumentName argName() {
    return variableElement.getAnnotation(ArgumentName.class);
  }

  String parameterName() {
    return variableElement.getSimpleName().toString();
  }

  OptionType optionType() {
    return optionType;
  }
}
