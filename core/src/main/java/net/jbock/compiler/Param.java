package net.jbock.compiler;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.STATIC;
import static net.jbock.compiler.Constants.JAVA_LANG_STRING;
import static net.jbock.compiler.Processor.checkNotPresent;
import static net.jbock.compiler.Util.asDeclared;
import static net.jbock.compiler.Util.asType;
import static net.jbock.compiler.Util.equalsType;
import static net.jbock.compiler.Util.methodToString;

import java.lang.annotation.Annotation;
import java.util.Arrays;
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

  final Type paramType;

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
    this.paramType = getParamType(sourceMethod);
  }

  private static Type getParamType(ExecutableElement sourceMethod) {
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
    basicChecks(sourceMethod);
    OtherTokens otherTokens = sourceMethod.getAnnotation(OtherTokens.class);
    EverythingAfter everythingAfter = sourceMethod.getAnnotation(EverythingAfter.class);
    if (otherTokens != null && everythingAfter != null) {
      throw new ValidationException(sourceMethod,
          "OtherTokens and EverythingAfter cannot be combined");
    }

    if (otherTokens != null) {
      return createOtherTokens(sourceMethod);
    }
    if (everythingAfter != null) {
      return createEverythingAfter(sourceMethod);
    }
    String longName = longName(sourceMethod);
    String shortName = shortName(sourceMethod);
    if (shortName == null && longName == null) {
      throw new ValidationException(sourceMethod,
          "Neither long nor short name defined for method " + Util.methodToString(sourceMethod));
    }
    checkName(sourceMethod, shortName);
    checkName(sourceMethod, longName);
    return new Param(
        shortName,
        longName,
        null,
        sourceMethod);
  }

  private static void basicChecks(ExecutableElement sourceMethod) {
    if (sourceMethod.getModifiers().contains(PRIVATE)) {
      throw new ValidationException(sourceMethod,
          "The method may not be private.");

    }
    if (sourceMethod.getModifiers().contains(STATIC)) {
      throw new ValidationException(sourceMethod,
          "Method " + methodToString(sourceMethod) +
              "may not be static.");

    }
    if (!sourceMethod.getParameters().isEmpty()) {
      throw new ValidationException(sourceMethod,
          "Method " + methodToString(sourceMethod) +
              " must have an empty parameter list.");
    }
    if (!sourceMethod.getTypeParameters().isEmpty()) {
      throw new ValidationException(sourceMethod,
          "Method " + methodToString(sourceMethod) +
              "must have type parameters.");
    }
    if (!sourceMethod.getThrownTypes().isEmpty()) {
      throw new ValidationException(sourceMethod,
          "Method " + methodToString(sourceMethod) +
              "may not declare any exceptions.");
    }
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

  private static void checkList(
      ExecutableElement sourceMethod, Annotation cause) {
    if (!isListOfString(sourceMethod.getReturnType())) {
      throw new ValidationException(sourceMethod,
          "The method that carries the " + cause.annotationType().getSimpleName() +
              " annotation must return List<String>");
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

  private static void checkName(
      ExecutableElement sourceMethod,
      String name) {
    if (name == null) {
      return;
    }
    basicCheckName(sourceMethod, name);
    if (name.indexOf(0) == '-') {
      throw new ValidationException(sourceMethod,
          "The name may not start with '-'");
    }
    if (name.indexOf('=') >= 0) {
      throw new ValidationException(sourceMethod,
          "The name may not contain '='");
    }
  }

  private static void basicCheckName(
      ExecutableElement sourceMethod,
      String name) {
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

  private static Param createEverythingAfter(ExecutableElement sourceMethod) {
    EverythingAfter everythingAfter = sourceMethod.getAnnotation(EverythingAfter.class);
    checkList(sourceMethod, everythingAfter);
    String stopword = everythingAfter.value();
    basicCheckName(sourceMethod, stopword);
    checkNotPresent(sourceMethod,
        everythingAfter,
        Arrays.asList(
            SuppressLongName.class,
            ShortName.class,
            LongName.class,
            OtherTokens.class));
    return new Param(
        null,
        null,
        stopword,
        sourceMethod);
  }

  private static Param createOtherTokens(ExecutableElement sourceMethod) {
    OtherTokens otherTokens = sourceMethod.getAnnotation(OtherTokens.class);
    checkList(sourceMethod, otherTokens);
    checkNotPresent(sourceMethod,
        otherTokens,
        Arrays.asList(
            SuppressLongName.class,
            ShortName.class,
            LongName.class,
            EverythingAfter.class));
    return new Param(
        null,
        null,
        null,
        sourceMethod);
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

  String methodName() {
    return sourceMethod.getSimpleName().toString();
  }

  boolean isSpecial() {
    return paramType.special;
  }

  String descriptionArgumentName() {
    if (!paramType.binding) {
      return null;
    }
    return description() == null ?
        "VAL" :
        description().argumentName();
  }
}
