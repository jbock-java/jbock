package net.jbock.qualifier;

import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;

import java.util.List;

public class NamedOptions {

  private final List<ConvertedParameter<NamedOption>> options;
  private final boolean anyRepeatable;
  private final boolean anyRegular;
  private final boolean anyFlags;

  private NamedOptions(
      List<ConvertedParameter<NamedOption>> options,
      boolean anyRepeatable,
      boolean anyRegular,
      boolean anyFlags) {
    this.options = options;
    this.anyRepeatable = anyRepeatable;
    this.anyRegular = anyRegular;
    this.anyFlags = anyFlags;
  }

  public static NamedOptions create(List<ConvertedParameter<NamedOption>> options) {
    boolean anyRepeatable = options.stream().anyMatch(ConvertedParameter::isRepeatable);
    boolean anyRegular = options.stream().anyMatch(option -> option.isOptional() || option.isRequired());
    boolean anyFlags = options.stream().anyMatch(ConvertedParameter::isFlag);
    return new NamedOptions(options, anyRepeatable, anyRegular, anyFlags);
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
}
