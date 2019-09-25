package net.jbock.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionProvider;
import net.jbock.coerce.InferredAttributes;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.lang.Character.isWhitespace;
import static net.jbock.compiler.AnnotationUtil.getCollectorClass;
import static net.jbock.compiler.AnnotationUtil.getMapperClass;

/**
 * Internal representation of an abstract method in the source class.
 */
final class Param {

  // can be null
  private final String longName;

  // can be blank
  private final char shortName;

  final OptionType paramType;

  final ExecutableElement sourceMethod;

  private final ParamName name;

  private final String bundleKey;

  private final Coercion coercion;

  private final List<String> description;

  private final String descriptionArgumentName;

  private final OptionalInt positionalIndex;

  boolean isFlag() {
    return paramType == OptionType.FLAG;
  }

  private static ParamName enumConstant(
      List<Param> params,
      ExecutableElement sourceMethod) {
    String methodName = sourceMethod.getSimpleName().toString();
    ParamName result = ParamName.create(methodName);
    for (Param param : params) {
      if (param.name.equals(result)) {
        return result.append(Integer.toString(params.size()));
      }
    }
    return result;
  }

  private static void checkBundleKey(
      String bundleKey,
      List<Param> params,
      ExecutableElement sourceMethod) {
    if (bundleKey.isEmpty()) {
      return;
    }
    for (int i = 0; i < bundleKey.length(); i++) {
      char c = bundleKey.charAt(i);
      if (Character.isWhitespace(c)) {
        throw ValidationException.create(sourceMethod,
            "The bundle key may not contain whitespace characters.");
      }
    }
    if (bundleKey.startsWith("jbock.")) {
      throw ValidationException.create(sourceMethod,
          "Bundle keys may not start with 'jbock.'.");
    }
    for (Param param : params) {
      if (param.bundleKey.isEmpty()) {
        continue;
      }
      if (param.bundleKey.equals(bundleKey)) {
        throw ValidationException.create(sourceMethod,
            "This bundle key is already taken.");
      }
    }
  }

  private static String descriptionArgumentName(
      OptionType paramType, boolean required, ParamName name) {
    if (paramType == OptionType.FLAG) {
      return null;
    }
    if (required) {
      return name.snake().toUpperCase();
    } else {
      return name.snake();
    }
  }

  private Param(
      char shortName,
      String longName,
      OptionType paramType,
      ExecutableElement sourceMethod,
      ParamName name,
      String bundleKey,
      Coercion coercion,
      List<String> description,
      String descriptionArgumentName,
      OptionalInt positionalIndex) {
    this.bundleKey = bundleKey;
    this.coercion = coercion;
    this.shortName = shortName;
    this.longName = longName;
    this.sourceMethod = sourceMethod;
    this.paramType = paramType;
    this.name = name;
    this.description = description;
    this.descriptionArgumentName = descriptionArgumentName;
    this.positionalIndex = positionalIndex;
    TypeTool tool = TypeTool.get();
    TypeMirror returnType = sourceMethod.getReturnType();
    boolean itsBoolean = tool.isSameType(returnType, tool.getPrimitiveType(TypeKind.BOOLEAN)) ||
        tool.isSameType(returnType, tool.asType(Boolean.class));
    if (paramType == OptionType.FLAG) {
      if (!itsBoolean) {
        throw ValidationException.create(sourceMethod, "Flag parameters must return boolean.");
      }
    }
  }

  FieldSpec field() {
    return coercion.field();
  }

  Coercion coercion() {
    return coercion;
  }

  static Param create(List<Param> params, ExecutableElement sourceMethod, OptionalInt positionalIndex, String[] description) {
    if (positionalIndex.isPresent()) {
      TypeElement mapperClass = getMapperClass(sourceMethod, PositionalParameter.class);
      TypeElement collectorClass = getCollectorClass(sourceMethod, PositionalParameter.class);
      return createPositional(params, sourceMethod, positionalIndex.getAsInt(), description, mapperClass, collectorClass);
    } else {
      TypeElement mapperClass = getMapperClass(sourceMethod, Parameter.class);
      TypeElement collectorClass = getCollectorClass(sourceMethod, Parameter.class);
      return createNonpositional(params, sourceMethod, description, mapperClass, collectorClass);
    }
  }

  private static Param createNonpositional(
      List<Param> params,
      ExecutableElement sourceMethod,
      String[] description,
      TypeElement mapperClass,
      TypeElement collectorClass) {
    TypeTool tool = TypeTool.get();
    String longName = longName(params, sourceMethod);
    char shortName = shortName(params, sourceMethod);
    if (shortName == ' ' && longName == null) {
      throw ValidationException.create(sourceMethod,
          "Define either long name or a short name");
    }
    Parameter parameter = sourceMethod.getAnnotation(Parameter.class);
    checkShortName(sourceMethod, shortName);
    checkName(sourceMethod, longName);
    ParamName name = enumConstant(params, sourceMethod);
    boolean flag = isInferredFlag(mapperClass, collectorClass, sourceMethod.getReturnType(), tool);
    InferredAttributes attributes = InferredAttributes.infer(sourceMethod.getReturnType(), tool);
    Coercion coercion;
    if (flag) {
      if (!parameter.descriptionArgumentName().isEmpty()) {
        throw ValidationException.create(sourceMethod,
            "A flag cannot have a description argument name.");
      }
      coercion = CoercionProvider.flagCoercion(sourceMethod, name);
    } else {
      coercion = CoercionProvider.findCoercion(sourceMethod, name, mapperClass, collectorClass, attributes, tool);
    }
    boolean repeatable = coercion.repeatable();
    boolean required = !repeatable && !coercion.optional() && !flag;
    OptionType type = optionType(repeatable, flag);
    String descriptionArgumentName = parameter.descriptionArgumentName().isEmpty() ?
        descriptionArgumentName(type, required, name) :
        parameter.descriptionArgumentName();
    checkBundleKey(parameter.bundleKey(), params, sourceMethod);
    return new Param(
        shortName,
        longName,
        type,
        sourceMethod,
        name,
        parameter.bundleKey(),
        coercion,
        cleanDesc(description),
        descriptionArgumentName,
        OptionalInt.empty());
  }

  private static Param createPositional(
      List<Param> params,
      ExecutableElement sourceMethod,
      int positionalIndex,
      String[] description,
      TypeElement mapperClass,
      TypeElement collectorClass) {
    TypeTool tool = TypeTool.get();
    PositionalParameter parameter = sourceMethod.getAnnotation(PositionalParameter.class);
    ParamName name = enumConstant(params, sourceMethod);
    InferredAttributes attributes = InferredAttributes.infer(sourceMethod.getReturnType(), tool);
    Coercion coercion = CoercionProvider.findCoercion(sourceMethod, name, mapperClass, collectorClass, attributes, tool);
    boolean repeatable = coercion.repeatable();
    boolean optional = coercion.optional();
    boolean required = !repeatable && !optional;
    OptionType type = optionType(repeatable, false);
    String descriptionArgumentName = parameter.descriptionArgumentName().isEmpty() ?
        descriptionArgumentName(type, required, name) :
        parameter.descriptionArgumentName();
    checkBundleKey(parameter.bundleKey(), params, sourceMethod);
    return new Param(
        ' ',
        null,
        type,
        sourceMethod,
        name,
        parameter.bundleKey(),
        coercion,
        cleanDesc(description),
        descriptionArgumentName,
        OptionalInt.of(positionalIndex));
  }

  /**
   * Can infer {@code flag = true}?
   */
  private static boolean isInferredFlag(
      Object mapperClass,
      Object collectorClass,
      TypeMirror mirror,
      TypeTool tool) {
    if (mapperClass != null || collectorClass != null) {
      // no inferring
      return false;
    }
    return isInferredFlag(tool, mirror);
  }

  private static boolean isInferredFlag(TypeTool tool, TypeMirror mirror) {
    return tool.isSameType(mirror, Boolean.class) || tool.isBooleanPrimitive(mirror);
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
        throw ValidationException.create(sourceMethod, "Duplicate short name");
      }
    }
    return c;
  }

  private static String longName(List<Param> params, ExecutableElement sourceMethod) {
    Parameter param = sourceMethod.getAnnotation(Parameter.class);
    if (param == null) {
      return null;
    }
    String longName = param.longName();
    if (longName.isEmpty()) {
      // the empty string indicates that no long name should be defined
      return null;
    }
    for (Param p : params) {
      if (p.longName != null && p.longName.equals(longName)) {
        throw ValidationException.create(sourceMethod, "Duplicate long name");
      }
    }
    return longName;
  }

  private static void checkShortName(
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
    if (name.isEmpty()) {
      throw ValidationException.create(sourceMethod,
          "The name may not be empty");
    }
    if (name.charAt(0) == '-') {
      throw ValidationException.create(sourceMethod,
          "The name may not start with '-'");
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (isWhitespace(c)) {
        throw ValidationException.create(sourceMethod,
            "The name may not contain whitespace characters");
      }
      if (c == '=') {
        throw ValidationException.create(sourceMethod,
            "The name may not contain '='");
      }
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

  TypeName returnType() {
    return TypeName.get(sourceMethod.getReturnType());
  }

  String enumConstant() {
    return name.snake().toUpperCase();
  }

  boolean isPositional() {
    return positionalIndex.isPresent();
  }

  boolean isOption() {
    return !isPositional();
  }

  OptionalInt positionalIndex() {
    return positionalIndex;
  }

  boolean required() {
    return !repeatable() && !optional() && !isFlag();
  }

  boolean repeatable() {
    return coercion.repeatable();
  }

  Optional<String> bundleKey() {
    return bundleKey.isEmpty() ? Optional.empty() : Optional.of(bundleKey);
  }

  PositionalRank positionalOrder() {
    if (!positionalIndex.isPresent()) {
      throw new AssertionError();
    }
    if (repeatable()) {
      return PositionalRank.LIST;
    }
    return optional() ? PositionalRank.OPTIONAL : PositionalRank.REQUIRED;
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

  private static OptionType optionType(boolean repeatable, boolean flag) {
    if (repeatable) {
      return OptionType.REPEATABLE;
    }
    if (flag) {
      return OptionType.FLAG;
    }
    return OptionType.REGULAR;
  }

  boolean optional() {
    return coercion.optional();
  }
}

