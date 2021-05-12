package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Context {

  private final List<ConvertedParameter<? extends AbstractParameter>> parameters;
  private final Optional<ConvertedParameter<PositionalParameter>> repeatableParam;
  private final List<ConvertedParameter<PositionalParameter>> regularParams;
  private final boolean anyRequired;

  @Inject
  Context(
      List<ConvertedParameter<NamedOption>> namedOptions,
      List<ConvertedParameter<PositionalParameter>> params) {
    this.parameters = ImmutableList.<ConvertedParameter<? extends AbstractParameter>>builderWithExpectedSize(namedOptions.size() + params.size())
        .addAll(namedOptions)
        .addAll(params).build();
    this.repeatableParam = params.stream()
        .filter(ConvertedParameter::isRepeatable)
        .findFirst();
    this.regularParams = params.stream()
        .filter(c -> !c.isRepeatable())
        .collect(Collectors.toList());
    this.anyRequired = parameters.stream().anyMatch(ConvertedParameter::isRequired);
  }

  public List<ConvertedParameter<? extends AbstractParameter>> parameters() {
    return parameters;
  }

  public boolean anyRepeatableParam() {
    return repeatableParam.isPresent();
  }

  public boolean anyRequired() {
    return anyRequired;
  }

  public Optional<ConvertedParameter<PositionalParameter>> repeatableParam() {
    return repeatableParam;
  }

  public List<ConvertedParameter<PositionalParameter>> regularParams() {
    return regularParams;
  }
}
