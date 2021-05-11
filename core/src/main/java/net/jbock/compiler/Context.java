package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  private final List<ConvertedParameter<PositionalParameter>> regularParams;

  private final List<ConvertedParameter<NamedOption>> options;

  private final ParserFlavour flavour;

  private final boolean unixClusteringSupported;

  @Inject
  Context(
      SourceElement sourceElement,
      ClassName generatedClass,
      List<ConvertedParameter<NamedOption>> namedOptions,
      List<ConvertedParameter<PositionalParameter>> params,
      ParserFlavour flavour) {
    this.sourceElement = sourceElement.element();
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
    this.regularParams = params.stream()
        .filter(c -> !c.isRepeatable())
        .collect(Collectors.toList());
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

  public List<ConvertedParameter<PositionalParameter>> regularParams() {
    return regularParams;
  }
}
