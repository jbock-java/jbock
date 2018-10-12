package net.jbock.compiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.Parameter;
import net.jbock.PositionalParameter;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionProvider;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Character.isWhitespace;
import static java.util.Collections.singletonList;
import static net.jbock.compiler.AnnotationUtil.getCollectorClass;
import static net.jbock.compiler.AnnotationUtil.getMapperClass;
import static net.jbock.compiler.Processor.checkNotPresent;
import static net.jbock.compiler.Util.snakeCase;

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

  private final String name;

  private final String bundleKey;

  private final Coercion coercion;

  private final List<String> description;

  private final String descriptionArgumentName;

  private final int positionalIndex;

  private final boolean optional;

  private final boolean repeatable;

  final boolean flag;

  private static String enumConstant(
      List<Param> params,
      ExecutableElement sourceMethod) {
    String methodName = sourceMethod.getSimpleName().toString();
    String result = snakeCase(methodName);
    for (Param param : params) {
      if (param.name.equals(result)) {
        return result + '_' + params.size();
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
      OptionType paramType, boolean required, ExecutableElement sourceMethod) {
    String name = snakeCase(sourceMethod.getSimpleName().toString());
    if (paramType == OptionType.FLAG) {
      return null;
    }
    if (required) {
      return name.toUpperCase();
    } else {
      return name;
    }
  }

  private Param(
      char shortName,
      String longName,
      OptionType paramType,
      ExecutableElement sourceMethod,
      String name,
      boolean positional,
      String bundleKey, Coercion coercion,
      List<String> description,
      String descriptionArgumentName,
      int positionalIndex,
      boolean optional,
      boolean repeatable,
      boolean flag) {
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
    this.optional = optional;
    this.repeatable = repeatable;
    this.flag = flag;
    TypeTool tt = TypeTool.get();
    TypeMirror returnType = sourceMethod.getReturnType();
    boolean itsBoolean = tt.eql(returnType, tt.primitive(TypeKind.BOOLEAN)) ||
        tt.eql(returnType, tt.declared(Boolean.class));
    if (flag) {
      if (!itsBoolean) {
        throw ValidationException.create(sourceMethod, "Flag parameters must return boolean.");
      }
    } else if (positionalIndex < 0) {
      if (itsBoolean && coercion.initMapper().isEmpty()) {
        throw ValidationException.create(sourceMethod, "Declare a flag, or use a custom mapper.");
      }
    }
    if (Stream.of(optional, repeatable, flag).mapToInt(Param::booleanToInt).sum() >= 2) {
      throw ValidationException.create(sourceMethod, "Only one of optional, repeatable and flag can be true.");
    }
    if (positional && positionalOrder() == null) {
      throw new AssertionError("positional, but positionalType is null");
    }
  }

  private static int booleanToInt(boolean b) {
    return b ? 1 : 0;
  }

  FieldSpec field() {
    return coercion.field();
  }

  Coercion coercion() {
    return coercion;
  }

  static Param create(List<Param> params, ExecutableElement sourceMethod, int positionalIndex, String[] description) {
    PositionalParameter positionalAnnotation = sourceMethod.getAnnotation(PositionalParameter.class);
    if (positionalAnnotation != null) {
      TypeElement mapperClass = getMapperClass(sourceMethod, PositionalParameter.class);
      TypeElement collectorClass = getCollectorClass(sourceMethod, PositionalParameter.class);
      return createPositional(params, sourceMethod, positionalIndex, description, mapperClass, collectorClass);
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
    String longName = longName(params, sourceMethod);
    char shortName = shortName(params, sourceMethod);
    if (shortName == ' ' && longName == null) {
      throw ValidationException.create(sourceMethod,
          "Define either long name or a short name");
    }
    Parameter parameter = sourceMethod.getAnnotation(Parameter.class);
    checkNotPresent(sourceMethod, parameter, singletonList(PositionalParameter.class));
    checkShortName(sourceMethod, shortName);
    checkName(sourceMethod, longName);
    String name = enumConstant(params, sourceMethod);
    boolean repeatable = parameter.repeatable();
    boolean optional = parameter.optional();
    boolean flag = parameter.flag();
    boolean required = !repeatable && !optional && !flag;
    Coercion typeInfo = CoercionProvider.getInstance().findCoercion(sourceMethod, name, mapperClass, collectorClass, repeatable, optional);
    OptionType type = optionType(repeatable, flag);
    String descriptionArgumentName = parameter.argHandle().isEmpty() ?
        descriptionArgumentName(type, required, sourceMethod) :
        parameter.argHandle();
    checkBundleKey(parameter.bundleKey(), params, sourceMethod);
    return new Param(
        shortName,
        longName,
        type,
        sourceMethod,
        name,
        false,
        parameter.bundleKey(),
        typeInfo,
        cleanDesc(description),
        descriptionArgumentName,
        -1,
        optional,
        repeatable,
        flag);
  }

  private static Param createPositional(
      List<Param> params,
      ExecutableElement sourceMethod,
      int positionalIndex,
      String[] description,
      TypeElement mapperClass,
      TypeElement collectorClass) {
    PositionalParameter parameter = sourceMethod.getAnnotation(PositionalParameter.class);
    String name = enumConstant(params, sourceMethod);
    boolean repeatable = parameter.repeatable();
    boolean optional = parameter.optional();
    boolean required = !repeatable && !optional;
    Coercion coercion = CoercionProvider.getInstance().findCoercion(sourceMethod, name, mapperClass, collectorClass, repeatable, optional);
    OptionType type = optionType(repeatable, false);
    checkNotPresent(sourceMethod, parameter, singletonList(Parameter.class));
    String descriptionArgumentName = parameter.argHandle().isEmpty() ?
        descriptionArgumentName(type, required, sourceMethod) :
        parameter.argHandle();
    checkBundleKey(parameter.bundleKey(), params, sourceMethod);
    return new Param(
        ' ',
        null,
        type,
        sourceMethod,
        name,
        true,
        parameter.bundleKey(),
        coercion,
        cleanDesc(description),
        descriptionArgumentName,
        positionalIndex,
        optional,
        repeatable,
        false);
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
    return name.toUpperCase();
  }

  boolean isPositional() {
    return positionalIndex >= 0;
  }

  boolean isOption() {
    return !isPositional();
  }

  int positionalIndex() {
    return positionalIndex;
  }

  boolean required() {
    return !repeatable && !optional && !flag;
  }

  Optional<String> bundleKey() {
    return bundleKey.isEmpty() ? Optional.empty() : Optional.of(bundleKey);
  }

  PositionalOrder positionalOrder() {
    if (repeatable) {
      return PositionalOrder.LIST;
    }
    return optional ? PositionalOrder.OPTIONAL : PositionalOrder.REQUIRED;
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
}

