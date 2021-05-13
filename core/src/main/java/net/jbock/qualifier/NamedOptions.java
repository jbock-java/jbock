package net.jbock.qualifier;

import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NamedOptions {

  private final List<ConvertedParameter<NamedOption>> options;
  private final List<ConvertedParameter<NamedOption>> requiredOptions;
  private final List<ConvertedParameter<NamedOption>> optionalOptions;
  private final boolean anyRepeatable;
  private final boolean anyRegular;
  private final boolean anyFlags;
  private final boolean unixClusteringSupported;
  private final int optionsWidth;


  private NamedOptions(
      List<ConvertedParameter<NamedOption>> options,
      List<ConvertedParameter<NamedOption>> requiredOptions,
      List<ConvertedParameter<NamedOption>> optionalOptions,
      boolean anyRepeatable,
      boolean anyRegular,
      boolean anyFlags,
      boolean unixClusteringSupported, int optionsWidth) {
    this.options = options;
    this.requiredOptions = requiredOptions;
    this.optionalOptions = optionalOptions;
    this.anyRepeatable = anyRepeatable;
    this.anyRegular = anyRegular;
    this.anyFlags = anyFlags;
    this.unixClusteringSupported = unixClusteringSupported;
    this.optionsWidth = optionsWidth;
  }

  public static NamedOptions create(List<ConvertedParameter<NamedOption>> options) {
    boolean anyRepeatable = options.stream().anyMatch(ConvertedParameter::isRepeatable);
    boolean anyRegular = options.stream().anyMatch(option -> option.isOptional() || option.isRequired());
    boolean anyFlags = options.stream().anyMatch(ConvertedParameter::isFlag);
    List<ConvertedParameter<NamedOption>> unixOptions = options.stream()
        .filter(option -> option.parameter().hasUnixName())
        .collect(Collectors.toList());
    boolean unixClusteringSupported = unixOptions.size() >= 2 && unixOptions.stream().anyMatch(ConvertedParameter::isFlag);
    Map<Boolean, List<ConvertedParameter<NamedOption>>> required = options.stream()
        .collect(Collectors.partitioningBy(ConvertedParameter::isRequired));
    int optionsWidth = options.stream()
        .map(c -> c.parameter().namesWithLabel(c.isFlag()))
        .mapToInt(String::length).max().orElse(0) + 3;
    return new NamedOptions(options, required.get(true), required.get(false),
        anyRepeatable, anyRegular, anyFlags, unixClusteringSupported, optionsWidth);
  }

  public boolean anyRepeatable() {
    return anyRepeatable;
  }

  public boolean anyRegular() {
    return anyRegular;
  }

  public boolean anyFlags() {
    return anyFlags;
  }

  public List<ConvertedParameter<NamedOption>> options() {
    return options;
  }

  public boolean isEmpty() {
    return options.isEmpty();
  }

  public Stream<ConvertedParameter<NamedOption>> stream() {
    return options.stream();
  }

  public void forEach(Consumer<ConvertedParameter<NamedOption>> consumer) {
    options.forEach(consumer);
  }

  public boolean unixClusteringSupported() {
    return unixClusteringSupported;
  }

  public List<ConvertedParameter<NamedOption>> required() {
    return requiredOptions;
  }

  public List<ConvertedParameter<NamedOption>> optional() {
    return optionalOptions;
  }

  public int optionsWidth() {
    return optionsWidth;
  }
}
