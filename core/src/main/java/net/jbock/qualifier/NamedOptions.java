package net.jbock.qualifier;

import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NamedOptions {

  private final List<ConvertedParameter<NamedOption>> options;
  private final boolean anyRepeatable;
  private final boolean anyRegular;
  private final boolean anyFlags;
  private final boolean unixClusteringSupported;


  private NamedOptions(
      List<ConvertedParameter<NamedOption>> options,
      boolean anyRepeatable,
      boolean anyRegular,
      boolean anyFlags,
      boolean unixClusteringSupported) {
    this.options = options;
    this.anyRepeatable = anyRepeatable;
    this.anyRegular = anyRegular;
    this.anyFlags = anyFlags;
    this.unixClusteringSupported = unixClusteringSupported;
  }

  public static NamedOptions create(List<ConvertedParameter<NamedOption>> options) {
    boolean anyRepeatable = options.stream().anyMatch(ConvertedParameter::isRepeatable);
    boolean anyRegular = options.stream().anyMatch(option -> option.isOptional() || option.isRequired());
    boolean anyFlags = options.stream().anyMatch(ConvertedParameter::isFlag);
    List<ConvertedParameter<NamedOption>> unixOptions = options.stream()
        .filter(option -> option.parameter().hasUnixName())
        .collect(Collectors.toList());
    boolean unixClusteringSupported = unixOptions.size() >= 2 && unixOptions.stream().anyMatch(ConvertedParameter::isFlag);
    return new NamedOptions(options, anyRepeatable, anyRegular, anyFlags, unixClusteringSupported);
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
}
