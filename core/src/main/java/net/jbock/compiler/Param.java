package net.jbock.compiler;

import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;
import static net.jbock.compiler.Util.equalsType;

import java.util.Objects;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import net.jbock.Description;
import net.jbock.EverythingAfter;
import net.jbock.LongName;
import net.jbock.OtherTokens;
import net.jbock.ShortName;
import net.jbock.SuppressLongName;

/**
 * Internal representation of an abstract method in the source class.
 */
final class Param {

  private final String longName;
  private final String shortName;

  final Type optionType;

  private final String stopword;

  final ExecutableElement sourceMethod;

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Param(
      String shortName,
      String longName,
      String stopword,
      ExecutableElement sourceMethod) {
    this.shortName = shortName;
    this.longName = longName;
    this.stopword = stopword;
    this.sourceMethod = sourceMethod;
    this.optionType = getOptionType(sourceMethod);
  }

  private static Type getOptionType(ExecutableElement executableElement) {
    if (executableElement.getAnnotation(OtherTokens.class) != null) {
      return Type.OTHER_TOKENS;
    }
    if (executableElement.getAnnotation(EverythingAfter.class) != null) {
      return Type.EVERYTHING_AFTER;
    }
    TypeMirror type = executableElement.getReturnType();
    if (type.getKind() == TypeKind.BOOLEAN) {
      return Type.FLAG;
    }
    if (isListOfString(type)) {
      return Type.REPEATABLE;
    }
    if (isOptionalString(type)) {
      return Type.OPTIONAL;
    }
    if (isString(type)) {
      return Type.REQUIRED;
    }
    String message = "Only String, Optional<String>, List<String> and boolean allowed, " +
        String.format("but %s() returns %s", executableElement.getSimpleName(), type);
    throw new ValidationException(message, executableElement);
  }

  static Param create(ExecutableElement parameter) {
    CreateHelper createHelper = new CreateHelper(parameter);
    if (parameter.getAnnotation(OtherTokens.class) != null) {
      return createHelper.createOtherTokens();
    }
    if (parameter.getAnnotation(EverythingAfter.class) != null) {
      return createHelper.createEverythingAfter();
    }
    String longName = longName(parameter);
    String shortName = shortName(parameter);
    if (shortName == null && longName == null) {
      throw new ValidationException("Neither long nor short name defined", parameter);
    }
    createHelper.checkName(shortName);
    createHelper.checkName(longName);
    return new Param(
        shortName,
        longName,
        null,
        parameter);
  }

  private static String shortName(ExecutableElement parameter) {
    ShortName shortName = parameter.getAnnotation(ShortName.class);
    return shortName != null ? Character.toString(shortName.value()) : null;
  }

  private static String longName(ExecutableElement parameter) {
    LongName longName = parameter.getAnnotation(LongName.class);
    if (parameter.getAnnotation(SuppressLongName.class) != null) {
      if (longName != null) {
        throw new ValidationException("LongName and SuppressLongName cannot be combined",
            parameter);
      }
      return null;
    }
    if (longName == null) {
      return parameter.getSimpleName().toString();
    }
    return longName.value();
  }

  private static void checkList(ExecutableElement variableElement) {
    if (!isListOfString(variableElement.getReturnType())) {
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

  private static boolean isString(
      TypeMirror type) {
    DeclaredType declared = type.accept(Util.AS_DECLARED, null);
    if (declared == null) {
      return false;
    }
    if (!declared.getTypeArguments().isEmpty()) {
      return false;
    }
    TypeElement element = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    return "java.lang.String".equals(element.getQualifiedName().toString());
  }

  private static final class CreateHelper {
    final ExecutableElement sourceMethod;

    CreateHelper(ExecutableElement sourceMethod) {
      this.sourceMethod = sourceMethod;
    }

    private void checkName(String name) {
      if (name == null) {
        return;
      }
      basicCheckName(name);
      if (name.startsWith("-")) {
        throw new ValidationException("The name may not start with '-'", sourceMethod);
      }
      if (name.indexOf('=') >= 0) {
        throw new ValidationException("The name may not contain '='", sourceMethod);
      }
    }

    private void basicCheckName(String name) {
      if (name == null) {
        throw new ValidationException("The name may not be null", sourceMethod);
      }
      if (name.isEmpty()) {
        throw new ValidationException("The name may not be empty", sourceMethod);
      }
      if (WHITE_SPACE.matcher(name).matches()) {
        throw new ValidationException("The name may not contain whitespace characters", sourceMethod);
      }
    }

    private Param createEverythingAfter() {
      checkList(sourceMethod);
      if (sourceMethod.getAnnotation(OtherTokens.class) != null) {
        throw new ValidationException(
            "EverythingAfter and OtherTokens cannot be combined", sourceMethod);
      }
      if (sourceMethod.getAnnotation(SuppressLongName.class) != null) {
        throw new ValidationException("EverythingAfter and SuppressLongName cannot be combined",
            sourceMethod);
      }
      if (sourceMethod.getAnnotation(LongName.class) != null) {
        throw new ValidationException(
            "EverythingAfter and LongName cannot be combined", sourceMethod);
      }
      if (sourceMethod.getAnnotation(ShortName.class) != null) {
        throw new ValidationException(
            "EverythingAfter and ShortName cannot be combined", sourceMethod);
      }
      String stopword = sourceMethod.getAnnotation(EverythingAfter.class).value();
      basicCheckName(stopword);
      return new Param(null,
          null,
          stopword,
          sourceMethod);
    }

    private Param createOtherTokens() {
      checkList(sourceMethod);
      if (sourceMethod.getAnnotation(EverythingAfter.class) != null) {
        throw new ValidationException(
            "OtherTokens and EverythingAfter cannot be combined", sourceMethod);
      }
      if (sourceMethod.getAnnotation(SuppressLongName.class) != null) {
        throw new ValidationException("EverythingAfter and SuppressLongName cannot be combined",
            sourceMethod);
      }
      if (sourceMethod.getAnnotation(LongName.class) != null) {
        throw new ValidationException(
            "OtherTokens and LongName cannot be combined", sourceMethod);
      }
      if (sourceMethod.getAnnotation(ShortName.class) != null) {
        throw new ValidationException(
            "OtherTokens and ShortName cannot be combined", sourceMethod);
      }
      return new Param(null,
          null,
          null,
          sourceMethod);
    }
  }

  String shortName() {
    return Objects.toString(shortName, null);
  }

  String longName() {
    return longName;
  }

  String stopword() {
    return stopword;
  }

  Description description() {
    return sourceMethod.getAnnotation(Description.class);
  }

  String parameterName() {
    return sourceMethod.getSimpleName().toString();
  }

  Type optionType() {
    return optionType;
  }

  boolean isSpecial() {
    return optionType.special;
  }

  String descriptionArgumentName() {
    if (!optionType.binding) {
      return null;
    }
    return description() == null ?
        "VAL" :
        description().argumentName();
  }
}
