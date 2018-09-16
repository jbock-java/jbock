package net.jbock.compiler;

import net.jbock.Parameter;
import net.jbock.PositionalParameter;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionProvider;
import net.jbock.coerce.TypeInfo;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.com.squareup.javapoet.TypeName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.compiler.Constants.OPTIONAL_DOUBLE;
import static net.jbock.compiler.Constants.OPTIONAL_INT;
import static net.jbock.compiler.Constants.OPTIONAL_LONG;
import static net.jbock.compiler.MapperClassUtil.getMapperClass;
import static net.jbock.compiler.OptionType.REPEATABLE;
import static net.jbock.compiler.Processor.checkNotPresent;
import static net.jbock.compiler.Util.snakeCase;

/**
 * Internal representation of an abstract method in the source class.
 */
final class Param {

  // can be null
  private final String longName;

  // can be null
  private final char shortName;

  // never null
  final OptionType paramType;

  // never null
  final ExecutableElement sourceMethod;

  // does it return string array
  final boolean isStringArray;

  final boolean required;

  private final String name;

  private final Coercion coercion;

  private final FieldSpec fieldSpec;

  private final List<String> description;

  private final String descriptionArgumentName;

  private final int positionalIndex;

  private static String enumConstant(
      List<Param> params,
      String methodName) {
    String result = snakeCase(methodName);
    for (Param param : params) {
      if (param.name.equals(result)) {
        return result + '_' + params.size();
      }
    }
    return result;
  }

  private static final Pattern WHITE_SPACE = Pattern.compile("^.*\\s+.*$");

  private Param(
      char shortName,
      String longName,
      OptionType paramType,
      ExecutableElement sourceMethod,
      boolean isStringArray,
      boolean required,
      String name,
      boolean positional,
      Coercion coercion,
      FieldSpec fieldSpec,
      List<String> description,
      String descriptionArgumentName,
      int positionalIndex) {
    this.required = required;
    this.coercion = coercion;
    this.shortName = shortName;
    this.longName = longName;
    this.sourceMethod = sourceMethod;
    this.paramType = paramType;
    this.isStringArray = isStringArray;
    this.name = name;
    this.fieldSpec = fieldSpec;
    this.description = description;
    this.descriptionArgumentName = descriptionArgumentName;
    this.positionalIndex = positionalIndex;
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

  static Param create(List<Param> params, ExecutableElement sourceMethod, int positionalIndex, String[] description) {
    PositionalParameter positionalAnnotation = sourceMethod.getAnnotation(PositionalParameter.class);
    if (positionalAnnotation != null) {
      TypeElement mapperClass = getMapperClass(sourceMethod, PositionalParameter.class);
      return createPositional(params, sourceMethod, positionalIndex, description, mapperClass);
    } else {
      TypeElement mapperClass = getMapperClass(sourceMethod, Parameter.class);
      return createNonpositional(params, sourceMethod, description, mapperClass);
    }
  }

  private static Param createNonpositional(
      List<Param> params,
      ExecutableElement sourceMethod,
      String[] description,
      TypeElement mapperClass) {
    String longName = longName(params, sourceMethod);
    char shortName = shortName(params, sourceMethod);
    if (shortName == ' ' && longName == null) {
      throw ValidationException.create(sourceMethod,
          "Neither long nor short name defined for method " + Util.methodToString(sourceMethod));
    }
    Parameter parameter = sourceMethod.getAnnotation(Parameter.class);
    checkNotPresent(sourceMethod, parameter, singletonList(PositionalParameter.class));
    checkName(sourceMethod, shortName);
    checkName(sourceMethod, longName);
    TypeInfo typeInfo = CoercionProvider.getInstance().findCoercion(sourceMethod, mapperClass);
    OptionType type = optionType(typeInfo);
    FieldSpec fieldSpec = FieldSpec.builder(type == REPEATABLE ?
            ParameterizedTypeName.get(ClassName.get(List.class), typeInfo.coercion().trigger()) :
            TypeName.get(sourceMethod.getReturnType()),
        sourceMethod.getSimpleName().toString())
        .addModifiers(FINAL)
        .build();
    String name = enumConstant(params, sourceMethod.getSimpleName().toString());
    String descriptionArgumentName = parameter.argHandle().isEmpty() ?
        descriptionArgumentName(type, typeInfo.required(), name) :
        parameter.argHandle();
    return new Param(
        shortName,
        longName,
        type,
        sourceMethod,
        typeInfo.array(),
        typeInfo.required(),
        name,
        false,
        typeInfo.coercion(),
        fieldSpec,
        cleanDesc(description),
        descriptionArgumentName,
        -1);
  }

  private static Param createPositional(
      List<Param> params,
      ExecutableElement sourceMethod,
      int positionalIndex,
      String[] description,
      TypeElement mapperClass) {
    PositionalParameter parameter = sourceMethod.getAnnotation(PositionalParameter.class);
    TypeInfo typeInfo = CoercionProvider.getInstance().findCoercion(sourceMethod, mapperClass);
    OptionType type = optionType(typeInfo);
    if (type == OptionType.FLAG) {
      throw ValidationException.create(sourceMethod,
          "A method that carries the Positional annotation " +
              "may not return " + TypeName.get(sourceMethod.getReturnType()));
    }
    checkNotPresent(sourceMethod, parameter, singletonList(Parameter.class));
    FieldSpec fieldSpec = FieldSpec.builder(type == REPEATABLE ?
            ParameterizedTypeName.get(ClassName.get(List.class), typeInfo.coercion().trigger()) :
            TypeName.get(sourceMethod.getReturnType()),
        sourceMethod.getSimpleName().toString())
        .addModifiers(FINAL)
        .build();
    String name = enumConstant(params, sourceMethod.getSimpleName().toString());
    String descriptionArgumentName = parameter.argHandle().isEmpty() ?
        descriptionArgumentName(type, typeInfo.required(), name) :
        parameter.argHandle();
    return new Param(
        ' ',
        null,
        type,
        sourceMethod,
        typeInfo.array(),
        typeInfo.required(),
        name,
        true,
        typeInfo.coercion(),
        fieldSpec,
        cleanDesc(description),
        descriptionArgumentName,
        positionalIndex);
  }

  private static char shortName(List<Param> params, ExecutableElement sourceMethod) {
    Parameter param = sourceMethod.getAnnotation(Parameter.class);
    if (param == null) {
      return ' ';
    }
    if (param.shortName() == ' ') {
      return ' ';
    }
    char c = param.shortName();
    for (Param p : params) {
      if (p.shortName == c) {
        throw ValidationException.create(sourceMethod,
            "Duplicate short name: " + c);
      }
    }
    return c;
  }

  private static String longName(List<Param> params, ExecutableElement sourceMethod) {
    Parameter param = sourceMethod.getAnnotation(Parameter.class);
    if (param == null) {
      if (sourceMethod.getAnnotation(PositionalParameter.class) == null) {
        throw ValidationException.create(sourceMethod,
            String.format("Expecting either %s or %s annotation",
                Parameter.class.getSimpleName(), PositionalParameter.class.getSimpleName()));
      }
      return null;
    }
    if (param.longName().isEmpty()) {
      // the empty string indicates that no long name should be defined
      return null;
    }
    String longName;
    if (param.longName().equals("-")) {
      longName = sourceMethod.getSimpleName().toString();
    } else {
      longName = param.longName();
    }
    for (Param p : params) {
      if (p.longName != null && p.longName.equals(longName)) {
        throw ValidationException.create(sourceMethod,
            "Duplicate long name: " + longName);
      }
    }
    return longName;
  }

  boolean isOptionalInt() {
    return OPTIONAL_INT.equals(returnType());
  }

  boolean isOptionalLong() {
    return OPTIONAL_LONG.equals(returnType());
  }

  boolean isOptionalDouble() {
    return OPTIONAL_DOUBLE.equals(returnType());
  }

  private static void checkName(
      ExecutableElement sourceMethod,
      char name) {
    if (name == ' ') {
      return;
    }
    checkName(sourceMethod, Character.toString(name));
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
    return shortName == ' ' ? null : shortName;
  }

  String longName() {
    return longName;
  }

  List<String> description() {
    return description;
  }

  String methodName() {
    return sourceMethod.getSimpleName().toString();
  }

  String descriptionArgumentName() {
    return descriptionArgumentName;
  }

  String descriptionArgumentNameWithDots() {
    if (paramType == OptionType.REPEATABLE) {
      return descriptionArgumentName + "...";
    }
    return descriptionArgumentName;
  }

  private static String descriptionArgumentName(
      OptionType paramType, boolean required, String name) {
    if (paramType == OptionType.FLAG) {
      return null;
    }
    if (required) {
      return name.toUpperCase();
    } else {
      return name;
    }
  }

  TypeName returnType() {
    return TypeName.get(sourceMethod.getReturnType());
  }

  String enumConstant() {
    return name.toUpperCase();
  }

  boolean isPositional() {
    return positionalIndex >= 0;
  }

  int positionalIndex() {
    return positionalIndex;
  }

  private CodeBlock missingRequiredOptionMessage(ClassName className) {
    if (isPositional()) {
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

  // visible for testing
  static List<String> cleanDesc(String[] desc) {
    if (desc.length == 0) {
      return Collections.emptyList();
    }
    String[] result = new String[desc.length];
    int resultpos = 0;
    for (String token : desc) {
      if (!token.startsWith("@")) {
        result[resultpos++] = token;
      }
    }
    return Arrays.asList(trim(Arrays.copyOf(result, resultpos)));
  }

  // visible for testing
  static String[] trim(String[] desc) {
    int firstNonempty = 0, lastNonempty = desc.length - 1;
    boolean nonemptyFound = false;
    for (int i = 0; i < desc.length; i++) {
      if (!desc[i].isEmpty()) {
        firstNonempty = i;
        nonemptyFound = true;
        break;
      }
    }
    if (!nonemptyFound) {
      return new String[0];
    }
    for (int j = desc.length - 1; j >= firstNonempty; j--) {
      if (!desc[j].isEmpty()) {
        lastNonempty = j;
        break;
      }
    }
    return Arrays.copyOfRange(desc, firstNonempty, lastNonempty + 1);
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
}

