package net.jbock.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionProvider;
import net.jbock.coerce.ParameterType;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Character.isWhitespace;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.compiler.AnnotationUtil.getCollectorClass;
import static net.jbock.compiler.AnnotationUtil.getMapperClass;

/**
 * Internal representation of an abstract method in the source class.
 */
public final class Param {

  private static final EnumSet<Modifier> NONPRIVATE_ACCESS_MODIFIERS =
      EnumSet.of(PUBLIC, PROTECTED);

  // null if absent
  private final String longName;

  // blank if absent
  private final char shortName;

  private final ExecutableElement sourceMethod;

  private final String bundleKey;

  private final Coercion coercion;

  private final List<String> description;

  private final String descriptionArgumentName;

  private final Integer positionalIndex;

  public boolean isFlag() {
    return coercion.parameterType().isFlag();
  }

  private static ParamName findParamName(
      List<Param> params,
      ExecutableElement sourceMethod) {
    String methodName = sourceMethod.getSimpleName().toString();
    ParamName result = ParamName.create(methodName);
    for (Param param : params) {
      if (param.paramName().equals(result)) {
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

  private static String descriptionArgumentName(ParameterType paramType, ParamName name) {
    if (paramType.isFlag()) {
      return null;
    }
    if (paramType.isRequired()) {
      return name.snake().toUpperCase(Locale.US);
    } else {
      return name.snake();
    }
  }

  private Param(
      char shortName,
      String longName,
      ExecutableElement sourceMethod,
      String bundleKey,
      Coercion coercion,
      List<String> description,
      String descriptionArgumentName,
      Integer positionalIndex) {
    this.bundleKey = bundleKey;
    this.coercion = coercion;
    this.shortName = shortName;
    this.longName = longName;
    this.sourceMethod = sourceMethod;
    this.description = description;
    this.descriptionArgumentName = descriptionArgumentName;
    this.positionalIndex = positionalIndex;
  }

  public FieldSpec field() {
    return coercion.field();
  }

  public Coercion coercion() {
    return coercion;
  }

  static Param create(TypeTool tool, List<Param> params, ExecutableElement sourceMethod, Integer positionalIndex, String[] description) {
    if (positionalIndex != null) {
      TypeElement mapperClass = getMapperClass(tool, sourceMethod, PositionalParameter.class);
      TypeElement collectorClass = getCollectorClass(tool, sourceMethod, PositionalParameter.class);
      return createPositional(params, sourceMethod, positionalIndex, description, mapperClass, collectorClass, tool);
    } else {
      TypeElement mapperClass = getMapperClass(tool, sourceMethod, Parameter.class);
      TypeElement collectorClass = getCollectorClass(tool, sourceMethod, Parameter.class);
      return createNonpositional(params, sourceMethod, description, mapperClass, collectorClass, tool);
    }
  }

  private static Param createNonpositional(
      List<Param> params,
      ExecutableElement sourceMethod,
      String[] description,
      TypeElement mapperClass,
      TypeElement collectorClass,
      TypeTool tool) {
    String longName = longName(params, sourceMethod);
    char shortName = shortName(params, sourceMethod);
    if (shortName == ' ' && longName == null) {
      throw ValidationException.create(sourceMethod,
          "Define either long name or a short name");
    }
    Parameter parameter = sourceMethod.getAnnotation(Parameter.class);
    checkShortName(sourceMethod, shortName);
    checkName(sourceMethod, longName);
    ParamName name = findParamName(params, sourceMethod);
    boolean flag = isInferredFlag(mapperClass, collectorClass, sourceMethod.getReturnType(), tool);
    Coercion coercion;
    if (flag) {
      if (!parameter.descriptionArgumentName().isEmpty()) {
        throw ValidationException.create(sourceMethod,
            "A flag cannot have a description argument name.");
      }
      coercion = CoercionProvider.flagCoercion(sourceMethod, name);
    } else {
      coercion = CoercionProvider.findCoercion(sourceMethod, name, mapperClass, collectorClass, tool);
    }
    String descriptionArgumentName = parameter.descriptionArgumentName().isEmpty() ?
        descriptionArgumentName(coercion.parameterType(), name) :
        parameter.descriptionArgumentName();
    checkBundleKey(parameter.bundleKey(), params, sourceMethod);
    return new Param(
        shortName,
        longName,
        sourceMethod,
        parameter.bundleKey(),
        coercion,
        cleanDesc(description),
        descriptionArgumentName,
        null);
  }

  private static Param createPositional(
      List<Param> params,
      ExecutableElement sourceMethod,
      int positionalIndex,
      String[] description,
      TypeElement mapperClass,
      TypeElement collectorClass,
      TypeTool tool) {
    PositionalParameter parameter = sourceMethod.getAnnotation(PositionalParameter.class);
    ParamName name = findParamName(params, sourceMethod);
    Coercion coercion = CoercionProvider.findCoercion(sourceMethod, name, mapperClass, collectorClass, tool);
    String descriptionArgumentName = parameter.descriptionArgumentName().isEmpty() ?
        descriptionArgumentName(coercion.parameterType(), name) :
        parameter.descriptionArgumentName();
    checkBundleKey(parameter.bundleKey(), params, sourceMethod);
    return new Param(
        ' ',
        null,
        sourceMethod,
        parameter.bundleKey(),
        coercion,
        cleanDesc(description),
        descriptionArgumentName,
        positionalIndex);
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
    return tool.isSameType(mirror, tool.getPrimitiveType(TypeKind.BOOLEAN)) ||
        tool.isSameType(mirror, Boolean.class);
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

  public Character shortName() {
    return shortName == ' ' ? null : shortName;
  }

  public String longName() {
    return longName;
  }

  public List<String> description() {
    return description;
  }

  public String methodName() {
    return sourceMethod.getSimpleName().toString();
  }

  public String descriptionArgumentName() {
    return descriptionArgumentName;
  }

  public String descriptionArgumentNameWithDots() {
    if (coercion.parameterType().isRepeatable()) {
      return descriptionArgumentName + "...";
    }
    return descriptionArgumentName;
  }

  public TypeName returnType() {
    return TypeName.get(sourceMethod.getReturnType());
  }

  public String enumConstant() {
    return paramName().snake().toUpperCase();
  }

  public boolean isPositional() {
    return positionalIndex != null;
  }

  public boolean isNotPositional() {
    return !isPositional();
  }

  public OptionalInt positionalIndex() {
    return positionalIndex != null ? OptionalInt.of(positionalIndex) : OptionalInt.empty();
  }

  public boolean isRequired() {
    return coercion.parameterType().isRequired();
  }

  public boolean isRepeatable() {
    return coercion.isRepeatable();
  }

  public Optional<String> bundleKey() {
    return bundleKey.isEmpty() ? Optional.empty() : Optional.of(bundleKey);
  }

  OptionalInt positionalOrder() {
    if (positionalIndex == null) {
      return OptionalInt.empty();
    }
    if (isRepeatable()) {
      return OptionalInt.of(2);
    }
    return isOptional() ? OptionalInt.of(1) : OptionalInt.of(0);
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

  public boolean isOptional() {
    return coercion.isOptional();
  }

  private ParamName paramName() {
    return coercion.paramName();
  }

  ValidationException validationError(String message) {
    return ValidationException.create(sourceMethod, message);
  }

  public Set<Modifier> getAccessModifiers() {
    return sourceMethod.getModifiers().stream()
        .filter(NONPRIVATE_ACCESS_MODIFIERS::contains)
        .collect(Collectors.toSet());
  }
}
