package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import net.jbock.Option;
import net.jbock.Param;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionProvider;
import net.jbock.coerce.FlagCoercion;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Character.isWhitespace;

class ParameterFactory {

  private final ExecutableElement sourceMethod;
  private final TypeElement sourceElement;
  private final TypeTool tool;
  private final ClassName optionType;

  @Inject
  ParameterFactory(ExecutableElement sourceMethod, TypeElement sourceElement, TypeTool tool, ClassName optionType) {
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.tool = tool;
    this.optionType = optionType;
  }

  Parameter createPositionalParam(List<Parameter> alreadyCreated, int positionalIndex, String[] description) {
    AnnotationUtil annotationUtil = new AnnotationUtil(tool, sourceMethod);
    Optional<TypeElement> mapperClass = annotationUtil.getMapper(Param.class);
    Param parameter = sourceMethod.getAnnotation(Param.class);
    ParamName name = findParamName(alreadyCreated);
    Coercion coercion = CoercionProvider.nonFlagCoercion(sourceMethod, sourceElement, name, mapperClass, optionType, tool);
    checkBundleKey(parameter.bundleKey(), alreadyCreated);
    return new Parameter(' ', null, sourceMethod, parameter.bundleKey(), name.snake().toLowerCase(Locale.US),
        Collections.emptyList(), coercion, Arrays.asList(description), positionalIndex);
  }

  Parameter createNamedOption(boolean anyMnemonics, List<Parameter> alreadyCreated, String[] description) {
    AnnotationUtil annotationUtil = new AnnotationUtil(tool, sourceMethod);
    Optional<TypeElement> mapperClass = annotationUtil.getMapper(Option.class);
    String optionName = optionName(alreadyCreated);
    char mnemonic = mnemonic(alreadyCreated);
    Option option = sourceMethod.getAnnotation(Option.class);
    ParamName name = findParamName(alreadyCreated);
    boolean flag = isInferredFlag(mapperClass);
    Coercion coercion = flag ?
        new FlagCoercion(name, sourceMethod) :
        CoercionProvider.nonFlagCoercion(sourceMethod, sourceElement, name, mapperClass, optionType, tool);
    checkBundleKey(option.bundleKey(), alreadyCreated);
    List<String> names = names(optionName, mnemonic);
    return new Parameter(mnemonic, optionName, sourceMethod, option.bundleKey(), sample(flag, name, names, anyMnemonics),
        names, coercion, Arrays.asList(description), null);
  }

  private ParamName findParamName(List<Parameter> alreadyCreated) {
    String methodName = sourceMethod.getSimpleName().toString();
    ParamName result = ParamName.create(methodName);
    for (Parameter param : alreadyCreated) {
      if (param.paramName().enumConstant().equals(result.enumConstant())) {
        return result.append(Integer.toString(alreadyCreated.size()));
      }
    }
    return result;
  }

  private void checkBundleKey(String bundleKey, List<Parameter> alreadyCreated) {
    if (bundleKey.isEmpty()) {
      return;
    }
    if (bundleKey.matches(".*\\s+.*")) {
      throw ValidationException.create(sourceMethod, "The bundle key may not contain whitespace characters.");
    }
    for (Parameter param : alreadyCreated) {
      if (bundleKey.equals(param.bundleKey)) {
        throw ValidationException.create(sourceMethod, "Duplicate bundle key.");
      }
    }
  }

  private boolean isInferredFlag(Optional<TypeElement> mapperClass) {
    if (mapperClass.isPresent()) {
      // not a flag
      return false;
    }
    TypeMirror mirror = sourceMethod.getReturnType();
    return mirror.getKind() == TypeKind.BOOLEAN || tool.isSameType(mirror, Boolean.class.getCanonicalName());
  }

  private String optionName(List<Parameter> params) {
    Option option = sourceMethod.getAnnotation(Option.class);
    if (option == null) {
      return null;
    }
    if (Objects.toString(option.value(), "").isEmpty()) {
      throw ValidationException.create(sourceMethod, "The name may not be empty");
    }
    for (Parameter p : params) {
      if (option.value().equals(p.optionName)) {
        throw ValidationException.create(sourceMethod, "Duplicate option name: " + option.value());
      }
    }
    return checkName(option.value());
  }

  private char checkMnemonic(char mnemonic) {
    if (mnemonic != ' ') {
      checkName(Character.toString(mnemonic));
    }
    return mnemonic;
  }

  private String checkName(String name) {
    if (Objects.toString(name, "").isEmpty()) {
      throw ValidationException.create(sourceMethod, "The name may not be empty");
    }
    if (name.charAt(0) == '-') {
      throw ValidationException.create(sourceMethod, "The name may not start with '-'");
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (isWhitespace(c)) {
        throw ValidationException.create(sourceMethod, "The name may not contain whitespace characters");
      }
      if (c == '=') {
        throw ValidationException.create(sourceMethod, "The name may not contain '='");
      }
    }
    return name;
  }

  private Character mnemonic(List<Parameter> parameters) {
    Option option = sourceMethod.getAnnotation(Option.class);
    if (option == null || option.mnemonic() == ' ') {
      return ' ';
    }
    for (Parameter p : parameters) {
      if (option.mnemonic() == p.mnemonic) {
        throw ValidationException.create(sourceMethod, "Duplicate mnemonic");
      }
    }
    return checkMnemonic(option.mnemonic());
  }

  private static List<String> names(String optionName, char mnemonic) {
    if (optionName != null && mnemonic == ' ') {
      return Collections.singletonList("--" + optionName);
    } else if (optionName == null && mnemonic != ' ') {
      return Collections.singletonList("-" + mnemonic);
    } else if (optionName == null) {
      return Collections.emptyList();
    }
    return Arrays.asList("-" + mnemonic, "--" + optionName);
  }

  private static String sample(boolean flag, ParamName name, List<String> names, boolean anyMnemonics) {
    if (names.isEmpty() || names.size() >= 3) {
      throw new AssertionError();
    }
    String argname = flag ? "" : ' ' + name.enumConstant();
    if (names.size() == 1) {
      // Note: The padding has the same length as the string "-f, "
      return (anyMnemonics ? "    " : "") + names.get(0) + argname;
    }
    return names.get(0) + ", " + names.get(1) + argname;
  }
}
