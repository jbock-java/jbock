package net.jbock.qualifier;

import com.google.common.collect.ImmutableList;
import net.jbock.compiler.Params;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.ConvertedParameter;

import java.util.List;

public final class AllParameters {

  private final List<ConvertedParameter<? extends AbstractParameter>> parameters;
  private final boolean anyRequired;

  private AllParameters(
      List<ConvertedParameter<? extends AbstractParameter>> parameters,
      boolean anyRequired) {
    this.parameters = parameters;
    this.anyRequired = anyRequired;
  }

  public static AllParameters create(Params params) {
    ImmutableList<ConvertedParameter<? extends AbstractParameter>> allParameters = ImmutableList.<ConvertedParameter<? extends AbstractParameter>>builderWithExpectedSize(
        params.namedOptions().size() + params.positionalParams().size())
        .addAll(params.namedOptions())
        .addAll(params.positionalParams()).build();
    boolean anyRequired = allParameters.stream().anyMatch(ConvertedParameter::isRequired);
    return new AllParameters(allParameters, anyRequired);
  }

  public List<ConvertedParameter<? extends AbstractParameter>> parameters() {
    return parameters;
  }

  public boolean anyRequired() {
    return anyRequired;
  }
}
