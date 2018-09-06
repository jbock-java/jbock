package net.jbock.compiler;

import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static net.jbock.compiler.Constants.JAVA_LANG_STRING;
import static net.jbock.compiler.Constants.JAVA_UTIL_OPTIONAL_INT;
import static net.jbock.compiler.Constants.OPTIONAL_INT;
import static net.jbock.compiler.Processor.checkNotPresent;
import static net.jbock.compiler.Util.asArray;
import static net.jbock.compiler.Util.asDeclared;
import static net.jbock.compiler.Util.asType;
import static net.jbock.compiler.Util.equalsType;
import static net.jbock.compiler.Util.methodToString;
import static net.jbock.compiler.Util.snakeCase;

/**
 * Internal representation of an abstract method in the source class.
 */
final class Param {

  // can be null
  private final String longName;

  // can be null
  private final String shortName;

  // never null
  final OptionType paramType;

  // index in the list of all abstract methods (in source order, ignoring inheritance)
  final int index;

  // never null
  final ExecutableElement sourceMethod;

  // does it return string array
  final boolean array;

  final boolean required;

  private final String name;

  private final boolean positional;

  final Coercion coercion;

  private static String enumConstant(List<Param> params, String methodName, int index) {
    String result = snakeCase(methodName);
    for (Param param : params) {
      if (param.name.equals(result)) {
        return result + '_' + index;
      }
    }
    return result;
  }


  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Param(
      String shortName,
      String longName,
      int index,
      OptionType paramType,
      ExecutableElement sourceMethod,
      boolean array,
      boolean required, String name,
      boolean positional,
      Coercion coercion) {
    this.required = required;
    this.coercion = coercion;
    this.shortName = shortName;
    this.longName = longName;
    this.index = index;
    this.sourceMethod = sourceMethod;
    this.paramType = paramType;
    this.array = array;
    this.name = name;
    this.positional = positional;
    if (positional && positionalOrder() == null) {
      throw new AssertionError("positional, but positionalType is null");
    }
  }

  CodeBlock extractExpression(Helper helper) {
    CodeBlock.Builder builder = paramType.extractExpression(helper, this).toBuilder();
    builder.add("$L", coercion.map());
    if (required) {
      builder.add("$L", orElseThrowMissing(helper.context));
    }
    return builder.build();
  }

  private CodeBlock orElseThrowMissing(Context context) {
    return CodeBlock.builder()
        .add("\n.orElseThrow(() -> new $T($L))", IllegalArgumentException.class,
            missingRequiredOptionMessage(context.optionType()))
        .build();
  }

  private static class OptionTypePlus {
    final OptionType type;
    final boolean required;

    OptionTypePlus(OptionType type, boolean required) {
      this.type = Objects.requireNonNull(type);
      this.required = required;
    }
  }

  private static OptionTypePlus optionType(OptionType type, boolean required) {
    return new OptionTypePlus(type, required);
  }

  private static OptionTypePlus checkNonpositionalType(ExecutableElement sourceMethod) {
    TypeMirror type = sourceMethod.getReturnType();
    if (type.getKind() == TypeKind.BOOLEAN) {
      return optionType(OptionType.FLAG, false);
    }
    if (type.getKind() == TypeKind.INT) {
      return optionType(OptionType.OPTIONAL, true);
    }
    if (isListOfString(type) || isStringArray(type)) {
      return optionType(OptionType.REPEATABLE, false);
    }
    if (isOptionalString(type)) {
      return optionType(OptionType.OPTIONAL, false);
    }
    if (isOptionalInt(type)) {
      return optionType(OptionType.OPTIONAL, false);
    }
    if (isString(type)) {
      return optionType(OptionType.OPTIONAL, true);
    }
    String message = String.format("Not allowed: %s() returns %s", sourceMethod.getSimpleName(), type);
    throw ValidationException.create(sourceMethod, message);
  }

  static Param create(List<Param> params, ExecutableElement sourceMethod, int index) {
    basicChecks(sourceMethod);
    if (sourceMethod.getAnnotation(Positional.class) == null) {
      return createNonpositional(params, sourceMethod, index);
    } else {
      return createPositional(params, sourceMethod, index);
    }
  }

  private static Param createNonpositional(List<Param> params, ExecutableElement sourceMethod, int index) {
    String longName = longName(sourceMethod);
    String shortName = shortName(sourceMethod);
    if (shortName == null && longName == null) {
      throw ValidationException.create(sourceMethod,
          "Neither long nor short name defined for method " + Util.methodToString(sourceMethod));
    }
    checkName(sourceMethod, shortName);
    checkName(sourceMethod, longName);
    OptionTypePlus type = checkNonpositionalType(sourceMethod);
    boolean array = type.type == OptionType.REPEATABLE && sourceMethod.getReturnType().getKind() == TypeKind.ARRAY;
    return new Param(
        shortName,
        longName,
        index,
        type.type,
        sourceMethod,
        array,
        type.required,
        enumConstant(params, sourceMethod.getSimpleName().toString(), index),
        false,
        Coercion.findCoercion(TypeName.get(sourceMethod.getReturnType())));
  }

  private static Param createPositional(List<Param> params, ExecutableElement sourceMethod, int index) {
    Positional positional = sourceMethod.getAnnotation(Positional.class);
    OptionTypePlus type = checkNonpositionalType(sourceMethod);
    if (type.type == OptionType.FLAG) {
      throw ValidationException.create(sourceMethod,
          "A method that carries the Positional annotation " +
              "may not return " + TypeName.get(sourceMethod.getReturnType()));
    }
    boolean array = type.type == OptionType.REPEATABLE && sourceMethod.getReturnType().getKind() == TypeKind.ARRAY;
    checkNotPresent(sourceMethod,
        positional,
        Arrays.asList(
            ShortName.class,
            LongName.class));
    return new Param(
        null,
        null,
        index,
        type.type,
        sourceMethod,
        array,
        type.required,
        enumConstant(params, sourceMethod.getSimpleName().toString(), index),
        true,
        Coercion.findCoercion(TypeName.get(sourceMethod.getReturnType())));
  }

  private static void basicChecks(ExecutableElement sourceMethod) {
    if (!sourceMethod.getParameters().isEmpty()) {
      throw ValidationException.create(sourceMethod,
          "Method " + methodToString(sourceMethod) +
              " may not have parameters.");
    }
    if (!sourceMethod.getTypeParameters().isEmpty()) {
      throw ValidationException.create(sourceMethod,
          "Method " + methodToString(sourceMethod) +
              "may not have type parameters.");
    }
    if (!sourceMethod.getThrownTypes().isEmpty()) {
      throw ValidationException.create(sourceMethod,
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
    if (longName == null) {
      return sourceMethod.getSimpleName().toString();
    }
    if (longName.value().isEmpty()) {
      // an empty string indicates that no long name should be defined
      return null;
    }
    return longName.value();
  }

  private static boolean isListOfString(TypeMirror type) {
    return isXOfString(type, "java.util.List");
  }

  private static boolean isOptionalString(TypeMirror type) {
    return isXOfString(type, "java.util.Optional");
  }

  private static boolean isStringArray(TypeMirror type) {
    if (type.getKind() != TypeKind.ARRAY) {
      return false;
    }
    ArrayType arrayType = asArray(type);
    if (arrayType == null) {
      return false;
    }
    return isString(arrayType.getComponentType());
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

  boolean isOptionalInt() {
    return OPTIONAL_INT.equals(returnType());
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
      throw ValidationException.create(sourceMethod,
          "The name may not start with '-'");
    }
    if (name.indexOf('=') >= 0) {
      throw ValidationException.create(sourceMethod,
          "The name may not contain '='");
    }
  }

  private static void basicCheckName(
      ExecutableElement sourceMethod,
      String name) {
    if (name == null) {
      throw ValidationException.create(sourceMethod,
          "The name may not be null");
    }
    if (name.isEmpty()) {
      throw ValidationException.create(sourceMethod,
          "The name may not be empty");
    }
    if (WHITE_SPACE.matcher(name).matches()) {
      throw ValidationException.create(sourceMethod,
          "The name may not contain whitespace characters");
    }
  }

  Character shortName() {
    return shortName == null ? null : shortName.charAt(0);
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

  String descriptionArgumentName() {
    if (paramType == OptionType.FLAG) {
      return null;
    }
    Description description = description();
    String result;
    if (description != null && !description.argumentName().isEmpty()) {
      result = description.argumentName();
    } else if (required) {
      result = name.toUpperCase();
    } else {
      result = name;
    }
    if (paramType == OptionType.REPEATABLE) {
      result += "...";
    }
    return result;
  }

  TypeName returnType() {
    return TypeName.get(sourceMethod.getReturnType());
  }

  String enumConstant() {
    return name.toUpperCase();
  }

  OptionType positionalType() {
    if (!positional) {
      throw new AssertionError("not positional");
    }
    return paramType;
  }

  boolean isPositional() {
    return positional;
  }

  private CodeBlock missingRequiredOptionMessage(ClassName className) {
    if (positional) {
      return CodeBlock.builder()
          .add("$T.format($S,$W$T.$L)",
              String.class,
              "Missing parameter: <%s>",
              className, enumConstant())
          .build();
    }
    return CodeBlock.builder()
        .add("$T.format($S,$W$T.$L,$W$T.$L.describeParam($S))",
            String.class,
            "Missing required option: %s (%s)",
            className, enumConstant(),
            className, enumConstant(),
            "")
        .build();
  }

  PositionalOrder positionalOrder() {
    switch (paramType) {
      case FLAG:
        return null;
      case OPTIONAL:
        return required ? PositionalOrder.REQUIRED : PositionalOrder.OPTIONAL;
      case REPEATABLE:
        return PositionalOrder.LIST;
      default:
        throw new AssertionError();
    }
  }
}
