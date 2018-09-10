package net.jbock.compiler;

import net.jbock.Description;
import net.jbock.LongName;
import net.jbock.Positional;
import net.jbock.ShortName;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionProvider;
import net.jbock.coerce.TypeInfo;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.compiler.Constants.OPTIONAL_INT;
import static net.jbock.compiler.Constants.OPTIONAL_LONG;
import static net.jbock.compiler.OptionType.REPEATABLE;
import static net.jbock.compiler.Processor.checkNotPresent;
import static net.jbock.compiler.Util.methodToString;
import static net.jbock.compiler.Util.snakeCase;

/**
 * Internal representation of an abstract method in the source class.
 */
public final class Param {

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

  private final Coercion coercion;

  private final FieldSpec fieldSpec;

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
      Coercion coercion,
      FieldSpec fieldSpec) {
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
    this.fieldSpec = fieldSpec;
    if (positional && positionalOrder() == null) {
      throw new AssertionError("positional, but positionalType is null");
    }
  }

  CodeBlock extractExpression(Helper helper) {
    CodeBlock.Builder builder = paramType.extractExpression(helper, this).toBuilder();
    if (paramType == REPEATABLE) {
      builder.add(".stream()");
    }
    builder.add("$L", coercion.map());
    if (paramType == REPEATABLE) {
      builder.add(".collect($T.toList())", Collectors.class);
    }
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

  FieldSpec field() {
    return fieldSpec;
  }

  Coercion coercion() {
    return coercion;
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
    TypeInfo typeInfo = CoercionProvider.getInstance().findCoercion(sourceMethod);
    OptionType type = optionType(typeInfo);
    FieldSpec fieldSpec = FieldSpec.builder(type == REPEATABLE ?
            ParameterizedTypeName.get(ClassName.get(List.class), typeInfo.coercion().trigger()) :
            TypeName.get(sourceMethod.getReturnType()),
        sourceMethod.getSimpleName().toString())
        .addModifiers(FINAL)
        .build();
    return new Param(
        shortName,
        longName,
        index,
        type,
        sourceMethod,
        typeInfo.array(),
        typeInfo.required(),
        enumConstant(params, sourceMethod.getSimpleName().toString(), index),
        false,
        typeInfo.coercion(),
        fieldSpec);
  }

  private static OptionType optionType(TypeInfo info) {
    if (info.flag()) {
      return OptionType.FLAG;
    }
    if (info.repeatable()) {
      return OptionType.REPEATABLE;
    }
    return OptionType.REGULAR;
  }

  private static Param createPositional(List<Param> params, ExecutableElement sourceMethod, int index) {
    Positional positional = sourceMethod.getAnnotation(Positional.class);
    TypeInfo typeInfo = CoercionProvider.getInstance().findCoercion(sourceMethod);
    OptionType type = optionType(typeInfo);
    if (type == OptionType.FLAG) {
      throw ValidationException.create(sourceMethod,
          "A method that carries the Positional annotation " +
              "may not return " + TypeName.get(sourceMethod.getReturnType()));
    }
    checkNotPresent(sourceMethod,
        positional,
        Arrays.asList(
            ShortName.class,
            LongName.class));
    FieldSpec fieldSpec = FieldSpec.builder(type == REPEATABLE ?
            ParameterizedTypeName.get(ClassName.get(List.class), typeInfo.coercion().trigger()) :
            TypeName.get(sourceMethod.getReturnType()),
        sourceMethod.getSimpleName().toString())
        .addModifiers(FINAL)
        .build();
    return new Param(
        null,
        null,
        index,
        type,
        sourceMethod,
        typeInfo.array(),
        typeInfo.required(),
        enumConstant(params, sourceMethod.getSimpleName().toString(), index),
        true,
        typeInfo.coercion(),
        fieldSpec);
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

  boolean isOptionalInt() {
    return OPTIONAL_INT.equals(returnType());
  }

  boolean isOptionalLong() {
    return OPTIONAL_LONG.equals(returnType());
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
      case REGULAR:
        return required ? PositionalOrder.REQUIRED : PositionalOrder.OPTIONAL;
      case REPEATABLE:
        return PositionalOrder.LIST;
      default:
        throw new AssertionError();
    }
  }
}
