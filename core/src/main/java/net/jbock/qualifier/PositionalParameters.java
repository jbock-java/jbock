package net.jbock.qualifier;

import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PositionalParameters {

  private final List<ConvertedParameter<PositionalParameter>> regular;
  private final Optional<ConvertedParameter<PositionalParameter>> repeatable;

  private PositionalParameters(
      List<ConvertedParameter<PositionalParameter>> regular,
      Optional<ConvertedParameter<PositionalParameter>> repeatable) {
    this.regular = regular;
    this.repeatable = repeatable;
  }

  public static PositionalParameters create(List<ConvertedParameter<PositionalParameter>> params) {
    List<ConvertedParameter<PositionalParameter>> regular = params.stream()
        .filter(c -> !c.isRepeatable())
        .collect(Collectors.toList());
    Optional<ConvertedParameter<PositionalParameter>> repeatable = params.stream()
        .filter(ConvertedParameter::isRepeatable)
        .findFirst();
    return new PositionalParameters(regular, repeatable);
  }

  public List<ConvertedParameter<PositionalParameter>> regular() {
    return regular;
  }

  public Optional<ConvertedParameter<PositionalParameter>> repeatable() {
    return repeatable;
  }

  public boolean anyRepeatable() {
    return repeatable.isPresent();
  }
}
