package net.jbock.compiler;

import static net.jbock.compiler.Constants.JAVA_LANG_STRING;
import static net.jbock.compiler.Util.asDeclared;
import static net.jbock.compiler.Util.asType;
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

  private static Type getOptionType(ExecutableElement sourceMethod) {
    if (sourceMethod.getAnnotation(OtherTokens.class) != null) {
      return Type.OTHER_TOKENS;
    }
    if (sourceMethod.getAnnotation(EverythingAfter.class) != null) {
      return Type.EVERYTHING_AFTER;
    }
    TypeMirror type = sourceMethod.getReturnType();
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
        String.format("but %s() returns %s", sourceMethod.getSimpleName(), type);
    throw new ValidationException(sourceMethod, message);
  }

  static Param create(ExecutableElement sourceMethod) {
    CreateHelper createHelper = new CreateHelper(sourceMethod);
    OtherTokens otherTokens = sourceMethod.getAnnotation(OtherTokens.class);
    EverythingAfter everythingAfter = sourceMethod.getAnnotation(EverythingAfter.class);
    if (otherTokens != null && everythingAfter != null) {
      throw new ValidationException(sourceMethod,
          "OtherTokens and EverythingAfter cannot be combined");
    }

    if (otherTokens != null) {
      return createHelper.createOtherTokens();
    }
    if (everythingAfter != null) {
      return createHelper.createEverythingAfter();
    }
    String longName = longName(sourceMethod);
    String shortName = shortName(sourceMethod);
    if (shortName == null && longName == null) {
      throw new ValidationException(sourceMethod,
          "Neither long nor short name defined");
    }
    createHelper.checkName(shortName);
    createHelper.checkName(longName);
    return new Param(
        shortName,
        longName,
        null,
        sourceMethod);
  }

  private static String shortName(ExecutableElement sourceMethod) {
    ShortName shortName = sourceMethod.getAnnotation(ShortName.class);
    return shortName != null ? Character.toString(shortName.value()) : null;
  }

  private static String longName(ExecutableElement sourceMethod) {
    LongName longName = sourceMethod.getAnnotation(LongName.class);
    if (sourceMethod.getAnnotation(SuppressLongName.class) != null) {
      if (longName != null) {
        throw new ValidationException(sourceMethod,
            "LongName and SuppressLongName cannot be combined");
      }
      return null;
    }
    if (longName == null) {
      return sourceMethod.getSimpleName().toString();
    }
    return longName.value();
  }

  private static void checkList(ExecutableElement sourceMethod) {
    if (!isListOfString(sourceMethod.getReturnType())) {
      throw new ValidationException(sourceMethod,
          "Must be a List<String>");
    }
  }

  private static boolean isListOfString(TypeMirror type) {
    return isXOfString(type, "java.util.List");
  }

  private static boolean isOptionalString(TypeMirror type) {
    return isXOfString(type, "java.util.Optional");
  }

  private static boolean isXOfString(
      TypeMirror type,
      String x) {
    DeclaredType declared = asDeclared(type);
    if (declared == null) {
      return false;
    }
    if (declared.getTypeArguments().size() != 1) {
      return false;
    }
    TypeElement typeElement = asType(declared.asElement());
    return x.equals(
        typeElement.getQualifiedName().toString()) &&
        equalsType(declared.getTypeArguments().get(0),
            JAVA_LANG_STRING);
  }

  private static boolean isString(
      TypeMirror type) {
    DeclaredType declared = asDeclared(type);
    if (declared == null) {
      return false;
    }
    if (!declared.getTypeArguments().isEmpty()) {
      return false;
    }
    TypeElement element = asType(declared.asElement());
    return JAVA_LANG_STRING.equals(
        element.getQualifiedName().toString());
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
      if (name.indexOf(0) == '-') {
        throw new ValidationException(sourceMethod,
            "The name may not start with '-'");
      }
      if (name.indexOf('=') >= 0) {
        throw new ValidationException(sourceMethod,
            "The name may not contain '='");
      }
    }

    private void basicCheckName(String name) {
      if (name == null) {
        throw new ValidationException(sourceMethod,
            "The name may not be null");
      }
      if (name.isEmpty()) {
        throw new ValidationException(sourceMethod,
            "The name may not be empty");
      }
      if (WHITE_SPACE.matcher(name).matches()) {
        throw new ValidationException(sourceMethod,
            "The name may not contain whitespace characters");
      }
    }

    private Param createEverythingAfter() {
      checkList(sourceMethod);
      String stopword = sourceMethod.getAnnotation(EverythingAfter.class).value();
      basicCheckName(stopword);
      return new Param(
          null,
          null,
          stopword,
          sourceMethod);
    }

    private Param createOtherTokens() {
      checkList(sourceMethod);
      return new Param(
          null,
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
