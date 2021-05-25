package net.jbock.context;

import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.parameter.AbstractParameter;
import net.jbock.validate.Params;

import java.util.List;

public final class AllParameters {

  private final List<ConvertedParameter<? extends AbstractParameter>> parameters;
  private final boolean anyRequired;
  private final boolean anyDescriptionKeys;

  private AllParameters(
      List<ConvertedParameter<? extends AbstractParameter>> parameters,
      boolean anyRequired,
      boolean anyDescriptionKeys) {
    this.parameters = parameters;
    this.anyRequired = anyRequired;
    this.anyDescriptionKeys = anyDescriptionKeys;
  }

  public static AllParameters create(Params params, Util util) {
    List<ConvertedParameter<? extends AbstractParameter>> allParameters =
        util.concat(params.namedOptions(), params.positionalParams());
    boolean anyRequired = allParameters.stream().anyMatch(ConvertedParameter::isRequired);
    boolean anyDescriptionKeys = allParameters.stream().anyMatch(c -> c.parameter().descriptionKey().isPresent());
    return new AllParameters(allParameters, anyRequired, anyDescriptionKeys);
  }

  public List<ConvertedParameter<? extends AbstractParameter>> parameters() {
    return parameters;
  }

  public boolean anyRequired() {
    return anyRequired;
  }

  public boolean anyDescriptionKeys() {
    return anyDescriptionKeys;
  }
}
