package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionProvider;
import net.jbock.coerce.ParameterStyle;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Character.isWhitespace;
import static net.jbock.compiler.Constants.NONPRIVATE_ACCESS_MODIFIERS;

/**
 * This class represents a parameter method (option or param).
 */
public final class Parameter {

  // null if absent
  private final String longName;

  // null if absent
  private final String shortName;

  private final ExecutableElement sourceMethod;

  private final String bundleKey;

  private final String shape;

  private final List<String> names;

  private final Coercion coercion;

  private final List<String> description;

  private final Integer positionalIndex;

  private static ParamName findParamName(
      List<Parameter> alreadyCreated,
      ExecutableElement sourceMethod) {
    String methodName = sourceMethod.getSimpleName().toString();
    ParamName result = ParamName.create(methodName);
    for (Parameter param : alreadyCreated) {
      if (param.paramName().enumConstant().equals(result.enumConstant())) {
        return result.append(Integer.toString(alreadyCreated.size()));
      }
    }
    return result;
  }

  private static void checkBundleKey(
      String bundleKey,
      List<Parameter> alreadyCreated,
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
    for (Parameter param : alreadyCreated) {
      if (param.bundleKey.isEmpty()) {
        continue;
      }
      if (param.bundleKey.equals(bundleKey)) {
        throw ValidationException.create(sourceMethod,
            "Duplicate bundle key.");
      }
    }
  }

  private Parameter(
      String shortName,
      String longName,
      ExecutableElement sourceMethod,
      String bundleKey,
      String shape,
      List<String> names,
      Coercion coercion,
      List<String> description,
      Integer positionalIndex) {
    this.bundleKey = bundleKey;
    this.shape = shape;
    this.names = names;
    this.coercion = coercion;
    this.shortName = shortName;
    this.longName = longName;
    this.sourceMethod = sourceMethod;
    this.description = description;
    this.positionalIndex = positionalIndex;
  }

  public FieldSpec field() {
    return coercion.field();
  }

  public Coercion coercion() {
    return coercion;
  }

  static Parameter create(boolean anyMnemonics, TypeTool tool, List<Parameter> alreadyCreated, ExecutableElement sourceMethod, Integer positionalIndex, String[] description, ClassName optionType) {
    AnnotationUtil annotationUtil = new AnnotationUtil(tool, sourceMethod);
    if (positionalIndex != null) {
      Optional<TypeElement> mapperClass = annotationUtil.get(net.jbock.Param.class, "mappedBy");
      Optional<TypeElement> collectorClass = annotationUtil.get(net.jbock.Param.class, "collectedBy");
      return createPositional(alreadyCreated, sourceMethod, positionalIndex, description, mapperClass, collectorClass, optionType, tool);
    } else {
      Optional<TypeElement> mapperClass = annotationUtil.get(Option.class, "mappedBy");
      Optional<TypeElement> collectorClass = annotationUtil.get(Option.class, "collectedBy");
      return createNonpositional(anyMnemonics, alreadyCreated, sourceMethod, description, mapperClass, collectorClass, optionType, tool);
    }
  }

  private static Parameter createNonpositional(
      boolean anyMnemonics,
      List<Parameter> params,
      ExecutableElement sourceMethod,
      String[] description,
      Optional<TypeElement> mapperClass,
      Optional<TypeElement> collectorClass,
      ClassName optionType,
      TypeTool tool) {
    String longName = longName(params, sourceMethod);
    String shortName = shortName(params, sourceMethod);
    if (shortName == null && longName == null) {
      throw ValidationException.create(sourceMethod, "Define either long name or a short name");
    }
    Option parameter = sourceMethod.getAnnotation(Option.class);
    checkShortName(sourceMethod, parameter.mnemonic());
    checkName(sourceMethod, parameter.value());
    ParamName name = findParamName(params, sourceMethod);
    boolean flag = isInferredFlag(mapperClass, collectorClass, sourceMethod.getReturnType(), tool);
    Coercion coercion = flag ?
        CoercionProvider.flagCoercion(sourceMethod, name) :
        CoercionProvider.findCoercion(sourceMethod, name, mapperClass, collectorClass, optionType, tool);
    checkBundleKey(parameter.value(), params, sourceMethod);
    List<String> names = names(longName, shortName);
    return new Parameter(
        shortName,
        longName,
        sourceMethod,
        parameter.value(),
        shape(flag, name, names, anyMnemonics),
        names,
        coercion,
        Arrays.asList(description),
        null);
  }

  private static Parameter createPositional(
      List<Parameter> alreadyCreated,
      ExecutableElement sourceMethod,
      int positionalIndex,
      String[] description,
      Optional<TypeElement> mapperClass,
      Optional<TypeElement> collectorClass,
      ClassName optionType,
      TypeTool tool) {
    net.jbock.Param parameter = sourceMethod.getAnnotation(net.jbock.Param.class);
    ParamName name = findParamName(alreadyCreated, sourceMethod);
    Coercion coercion = CoercionProvider.findCoercion(sourceMethod, name, mapperClass, collectorClass, optionType, tool);
    checkBundleKey(parameter.bundleKey(), alreadyCreated, sourceMethod);
    return new Parameter(
        null,
        null,
        sourceMethod,
        parameter.bundleKey(),
        name.snake().toLowerCase(Locale.US),
        Collections.emptyList(),
        coercion,
        Arrays.asList(description),
        positionalIndex);
  }

  private static boolean isInferredFlag(
      Optional<TypeElement> mapperClass,
      Optional<TypeElement> collectorClass,
      TypeMirror mirror,
      TypeTool tool) {
    if (mapperClass.isPresent() || collectorClass.isPresent()) {
      // no inferring
      return false;
    }
    return tool.isSameType(mirror, tool.getPrimitiveType(TypeKind.BOOLEAN)) ||
        tool.isSameType(mirror, Boolean.class);
  }

  private static String shortName(List<Parameter> params, ExecutableElement sourceMethod) {
    Option param = sourceMethod.getAnnotation(Option.class);
    if (param == null) {
      return null;
    }
    if (param.mnemonic() == ' ') {
      return null;
    }
    String result = "-" + param.mnemonic();
    for (Parameter p : params) {
      if (result.equals(p.shortName)) {
        throw ValidationException.create(sourceMethod, "Duplicate short name");
      }
    }
    return result;
  }

  private static String longName(List<Parameter> params, ExecutableElement sourceMethod) {
    Option param = sourceMethod.getAnnotation(Option.class);
    if (param == null) {
      return null;
    }
    if (Objects.toString(param.value(), "").isEmpty()) {
      throw ValidationException.create(sourceMethod,
          "The name may not be empty");
    }
    String longName = "--" + param.value();
    for (Parameter p : params) {
      if (p.longName != null && p.longName.equals(longName)) {
        throw ValidationException.create(sourceMethod, "Duplicate long name");
      }
    }
    return longName;
  }

  private static void checkShortName(ExecutableElement sourceMethod, char name) {
    if (name == ' ') {
      return;
    }
    checkName(sourceMethod, Character.toString(name));
  }

  private static void checkName(ExecutableElement sourceMethod, String name) {
    if (Objects.toString(name, "").isEmpty()) {
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

  public Optional<String> longName() {
    return Optional.ofNullable(longName);
  }

  public List<String> description() {
    return description;
  }

  public String methodName() {
    return sourceMethod.getSimpleName().toString();
  }

  public TypeName returnType() {
    return TypeName.get(sourceMethod.getReturnType());
  }

  public String enumConstant() {
    return paramName().enumConstant();
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
    return coercion.getStyle().isPresent() && coercion.getStyle().get() == ParameterStyle.REQUIRED;
  }

  public boolean isRepeatable() {
    return coercion.getStyle().isPresent() && coercion.getStyle().get() == ParameterStyle.REPEATABLE;
  }

  public boolean isOptional() {
    return coercion.getStyle().isPresent() && coercion.getStyle().get() == ParameterStyle.OPTIONAL;
  }

  public boolean isFlag() {
    return !coercion.getStyle().isPresent();
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

  public ParamName paramName() {
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

  public List<String> names() {
    return names;
  }

  static List<String> names(String longName, String shortName) {
    if (longName != null && shortName == null) {
      return Collections.singletonList(longName);
    } else if (longName == null && shortName != null) {
      return Collections.singletonList(shortName);
    } else if (longName == null) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(shortName, longName);
    }
  }

  public String shape() {
    return shape;
  }

  private static String shape(
      boolean flag,
      ParamName name,
      List<String> names,
      boolean anyMnemonics) {
    if (names.isEmpty() || names.size() >= 3) {
      throw new AssertionError();
    }
    String argname = flag ? "" : ' ' + name.enumConstant();
    if (names.size() == 1) {
      // The padding has the same length as the string "-f, "
      String padding = anyMnemonics ? "    " : "";
      return padding + names.get(0) + argname;
    }
    return names.get(0) + ", " + names.get(1) + argname;
  }
}
