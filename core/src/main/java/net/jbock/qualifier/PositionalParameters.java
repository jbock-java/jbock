package net.jbock.qualifier;

import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PositionalParameters {

  private final List<ConvertedParameter<PositionalParameter>> all;
  private final List<ConvertedParameter<PositionalParameter>> regular;
  private final Optional<ConvertedParameter<PositionalParameter>> repeatable;

  private final int paramsWidth;

  private PositionalParameters(
      List<ConvertedParameter<PositionalParameter>> all,
      List<ConvertedParameter<PositionalParameter>> regular,
      Optional<ConvertedParameter<PositionalParameter>> repeatable,
      int paramsWidth) {
    this.all = all;
    this.regular = regular;
    this.repeatable = repeatable;
    this.paramsWidth = paramsWidth;
  }

  public static PositionalParameters create(List<ConvertedParameter<PositionalParameter>> all) {
    List<ConvertedParameter<PositionalParameter>> regular = all.stream()
        .filter(c -> !c.isRepeatable())
        .collect(Collectors.toList());
    Optional<ConvertedParameter<PositionalParameter>> repeatable = all.stream()
        .filter(ConvertedParameter::isRepeatable)
        .findFirst();
    return new PositionalParameters(all, regular, repeatable, all.stream()
        .map(ConvertedParameter::parameter)
        .map(PositionalParameter::paramLabel)
        .mapToInt(String::length).max().orElse(0) + 3);
  }

  public List<ConvertedParameter<PositionalParameter>> regular() {
    return regular;
  }

  public List<ConvertedParameter<PositionalParameter>> all() {
    return all;
  }

  public int size() {
    return all.size();
  }

  public Optional<ConvertedParameter<PositionalParameter>> repeatable() {
    return repeatable;
  }

  public boolean anyRepeatable() {
    return repeatable.isPresent();
  }

  public boolean isEmpty() {
    return all.isEmpty();
  }

  public void forEach(Consumer<ConvertedParameter<PositionalParameter>> consumer) {
    all.forEach(consumer);
  }

  public int paramsWidth() {
    return paramsWidth;
  }
}
