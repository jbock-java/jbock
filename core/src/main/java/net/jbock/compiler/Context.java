package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.convert.ConvertedParameter;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.ALLOWED_MODIFIERS;

public final class Context {

  // the annotated class
  private final TypeElement sourceElement;

  // the class that will be generated
  private final ClassName generatedClass;

  // the abstract methods in the annotated class
  private final List<ConvertedParameter<? extends AbstractParameter>> parameters;

  private final List<ConvertedParameter<PositionalParameter>> params;

  private final Optional<ConvertedParameter<PositionalParameter>> repeatableParam;

  private final List<ConvertedParameter<? extends AbstractParameter>> regularParameters;

  private final List<ConvertedParameter<PositionalParameter>> regularParams;

  private final List<ConvertedParameter<NamedOption>> options;

  private final ParserFlavour flavour;

  private final GeneratedTypes generatedTypes;

  private final boolean unixClusteringSupported;

  @Inject
  Context(
      TypeElement sourceElement,
      ClassName generatedClass,
      List<ConvertedParameter<NamedOption>> namedOptions,
      List<ConvertedParameter<PositionalParameter>> params,
      ParserFlavour flavour,
      GeneratedTypes generatedTypes) {
    this.sourceElement = sourceElement;
    this.generatedClass = generatedClass;
    this.params = params;
    this.options = namedOptions;
    this.unixClusteringSupported = isUnixClusteringSupported(namedOptions);
    this.flavour = flavour;
    this.parameters = ImmutableList.<ConvertedParameter<? extends AbstractParameter>>builder()
        .addAll(options)
        .addAll(params).build();
    this.repeatableParam = params.stream()
        .filter(ConvertedParameter::isRepeatable)
        .findFirst();
    this.regularParameters = parameters.stream()
        .filter(c -> !(c.parameter().isPositional() && c.isRepeatable()))
        .collect(Collectors.toList());
    this.regularParams = params.stream()
        .filter(c -> !c.isRepeatable())
        .collect(Collectors.toList());
    this.generatedTypes = generatedTypes;
  }

  public Modifier[] getAccessModifiers() {
    return sourceElement.getModifiers().stream().filter(ALLOWED_MODIFIERS::contains).toArray(Modifier[]::new);
  }

  public ClassName generatedClass() {
    return generatedClass;
  }

  public List<ConvertedParameter<? extends AbstractParameter>> parameters() {
    return parameters;
  }

  public List<ConvertedParameter<PositionalParameter>> params() {
    return params;
  }

  public List<ConvertedParameter<NamedOption>> options() {
    return options;
  }

  public boolean isHelpParameterEnabled() {
    return flavour.helpEnabled(sourceElement);
  }

  public String programName() {
    return flavour.programName(sourceElement);
  }

  public FieldSpec exitHookField() {
    ParameterizedTypeName consumer = ParameterizedTypeName.get(ClassName.get(Consumer.class),
        generatedTypes.parseResultType());
    ParameterSpec result = ParameterSpec.builder(generatedTypes.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();
    if (generatedTypes.helpRequestedType().isPresent()) {
      generatedTypes.helpRequestedType().ifPresent(helpRequestedType -> {
        code.add("$N ->\n", result).indent()
            .add("$T.exit($N instanceof $T ? 0 : 1)", System.class, result, helpRequestedType)
            .unindent();
      });
    } else {
      code.add("$N -> $T.exit(1)", result, System.class);
    }
    return FieldSpec.builder(consumer, "exitHook")
        .addModifiers(PRIVATE)
        .initializer(code.build())
        .build();
  }

  public boolean isSuperCommand() {
    return flavour.isSuperCommand();
  }

  public boolean anyRepeatableParam() {
    return repeatableParam.isPresent();
  }

  public boolean isUnixClusteringSupported() {
    return unixClusteringSupported;
  }

  private static boolean isUnixClusteringSupported(List<ConvertedParameter<NamedOption>> options) {
    List<ConvertedParameter<NamedOption>> unixOptions = options.stream()
        .filter(option -> option.parameter().hasUnixName())
        .collect(Collectors.toList());
    return unixOptions.size() >= 2 && unixOptions.stream().anyMatch(ConvertedParameter::isFlag);
  }

  public String getSuccessResultMethodName() {
    return isSuperCommand() ? "getResultWithRest" : "getResult";
  }

  public Optional<ConvertedParameter<PositionalParameter>> repeatableParam() {
    return repeatableParam;
  }

  /**
   * Everything but the repeatable param.
   */
  public List<ConvertedParameter<? extends AbstractParameter>> regularParameters() {
    return regularParameters;
  }

  public List<ConvertedParameter<PositionalParameter>> regularParams() {
    return regularParams;
  }
}
