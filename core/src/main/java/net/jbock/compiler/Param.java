package net.jbock.compiler;

import static net.jbock.compiler.Constants.JAVA_LANG_STRING;
import static net.jbock.compiler.Constants.JAVA_UTIL_OPTIONAL_INT;
import static net.jbock.compiler.Processor.checkNotPresent;
import static net.jbock.compiler.Util.asDeclared;
import static net.jbock.compiler.Util.asType;
import static net.jbock.compiler.Util.equalsType;
import static net.jbock.compiler.Util.methodToString;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;
import net.jbock.SuppressLongName;
import net.jbock.com.squareup.javapoet.TypeName;

/**
 * Internal representation of an abstract method in the source class.
 */
final class Param {

  private final String longName;
  private final String shortName;

  final Type paramType;

  // index in the list of abstract methods
  final int index;

  final ExecutableElement sourceMethod;

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Param(
      String shortName,
      String longName,
      int index,
      Type paramType,
      ExecutableElement sourceMethod) {
    this.shortName = shortName;
    this.longName = longName;
    this.index = index;
    this.sourceMethod = sourceMethod;
    this.paramType = paramType;
  }

  private static Type getParamType(ExecutableElement sourceMethod) {
    if (sourceMethod.getAnnotation(Positional.class) != null) {
      return Type.POSITIONAL_LIST;
    }
    TypeMirror type = sourceMethod.getReturnType();
    if (type.getKind() == TypeKind.BOOLEAN) {
      return Type.FLAG;
    }
    if (type.getKind() == TypeKind.INT) {
      return Type.REQUIRED_INT;
    }
    if (isListOfString(type)) {
      return Type.REPEATABLE;
    }
    if (isOptionalString(type)) {
      return Type.OPTIONAL;
    }
    if (isOptionalInt(type)) {
      return Type.OPTIONAL_INT;
    }
    if (isString(type)) {
      return Type.REQUIRED;
    }
    Set<String> allowed = Arrays.stream(Type.values())
        .map(t -> t.returnType)
        .map(TypeName::toString)
        .collect(Collectors.toSet());
    String message = String.format("Allowed return types: [" +
        String.join(", ", allowed) +
        "], but %s() returns %s", sourceMethod.getSimpleName(), type);
    throw new ValidationException(sourceMethod, message);
  }

  static Param create(ExecutableElement sourceMethod, int index) {
    basicChecks(sourceMethod);
    Positional positional = sourceMethod.getAnnotation(Positional.class);

    if (positional != null) {
      return createOtherTokens(sourceMethod, index);
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
        index,
        getParamType(sourceMethod),
        sourceMethod);
  }

  private static void basicChecks(ExecutableElement sourceMethod) {
    if (!sourceMethod.getParameters().isEmpty()) {
      throw new ValidationException(sourceMethod,
          "Method " + methodToString(sourceMethod) +
              " may not have parameters.");
    }
    if (!sourceMethod.getTypeParameters().isEmpty()) {
      throw new ValidationException(sourceMethod,
          "Method " + methodToString(sourceMethod) +
              "may not have type parameters.");
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

  private static Type checkOptionalType(
      ExecutableElement sourceMethod, Annotation cause) {
    if (isListOfString(sourceMethod.getReturnType())) {
      return Type.POSITIONAL_LIST;
    }
    if (isString(sourceMethod.getReturnType())) {
      return Type.REQUIRED_POSITIONAL;
    }
    if (isOptionalString(sourceMethod.getReturnType())) {
      return Type.OPTIONAL_POSITIONAL;
    }
    throw new ValidationException(sourceMethod,
        "A method that carries the " + cause.annotationType().getSimpleName() +
            " annotation must return String, Optional<String> or List<String>");
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
    return isSimpleType(type, JAVA_LANG_STRING);
  }

  private static boolean isOptionalInt(
      TypeMirror type) {
    return isSimpleType(type, JAVA_UTIL_OPTIONAL_INT);
  }

  private static boolean isSimpleType(TypeMirror type, String qname) {
    DeclaredType declared = asDeclared(type);
    if (declared == null) {
      return false;
    }
    if (!declared.getTypeArguments().isEmpty()) {
      return false;
    }
    TypeElement element = asType(declared.asElement());
    return qname.equals(element.getQualifiedName().toString());
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

  private static Param createOtherTokens(ExecutableElement sourceMethod, int index) {
    Positional positional = sourceMethod.getAnnotation(Positional.class);
    Type type = checkOptionalType(sourceMethod, positional);
    checkNotPresent(sourceMethod,
        positional,
        Arrays.asList(
            SuppressLongName.class,
            ShortName.class,
            LongName.class));
    return new Param(
        null,
        null,
        index,
        type,
        sourceMethod);
  }

  String shortName() {
    return Objects.toString(shortName, null);
  }

  String longName() {
    return longName;
  }

  Description description() {
    return sourceMethod.getAnnotation(Description.class);
  }

  String methodName() {
    return sourceMethod.getSimpleName().toString();
  }

  Param asPositional2() {
    return new Param(shortName, longName, index, Type.POSITIONAL_LIST_2, sourceMethod);
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
