package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Context {

  private final SourceElement sourceElement;
  private final GeneratedType generatedType;
  private final List<ConvertedParameter<? extends AbstractParameter>> parameters;
  private final Optional<ConvertedParameter<PositionalParameter>> repeatableParam;
  private final List<ConvertedParameter<PositionalParameter>> regularParams;
  private final List<ConvertedParameter<NamedOption>> options;
  private final boolean unixClusteringSupported;
  private final boolean anyRequired;

  @Inject
  Context(
      SourceElement sourceElement,
      GeneratedType generatedType,
      List<ConvertedParameter<NamedOption>> namedOptions,
      List<ConvertedParameter<PositionalParameter>> params) {
    this.sourceElement = sourceElement;
    this.generatedType = generatedType;
    this.options = namedOptions;
    this.unixClusteringSupported = isUnixClusteringSupported(namedOptions);
    this.parameters = ImmutableList.<ConvertedParameter<? extends AbstractParameter>>builderWithExpectedSize(options.size() + params.size())
        .addAll(options)
        .addAll(params).build();
    this.repeatableParam = params.stream()
        .filter(ConvertedParameter::isRepeatable)
        .findFirst();
    this.regularParams = params.stream()
        .filter(c -> !c.isRepeatable())
        .collect(Collectors.toList());
    this.anyRequired = parameters.stream().anyMatch(ConvertedParameter::isRequired);
  }

  public GeneratedType generatedType() {
    return generatedType;
  }

  public List<ConvertedParameter<? extends AbstractParameter>> parameters() {
    return parameters;
  }

  public boolean isHelpParameterEnabled() {
    return sourceElement.helpEnabled();
  }

  public boolean isSuperCommand() {
    return sourceElement.isSuperCommand();
  }

  public boolean anyRepeatableParam() {
    return repeatableParam.isPresent();
  }

  public boolean anyRequired() {
    return anyRequired;
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
