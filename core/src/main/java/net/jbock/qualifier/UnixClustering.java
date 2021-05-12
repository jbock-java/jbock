package net.jbock.qualifier;

import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;

import java.util.List;
import java.util.stream.Collectors;

public class UnixClustering {

  private final boolean supported;

  public UnixClustering(boolean supported) {
    this.supported = supported;
  }

  public static UnixClustering create(List<ConvertedParameter<NamedOption>> options) {
    List<ConvertedParameter<NamedOption>> unixOptions = options.stream()
        .filter(option -> option.parameter().hasUnixName())
        .collect(Collectors.toList());
    boolean supported = unixOptions.size() >= 2 && unixOptions.stream().anyMatch(ConvertedParameter::isFlag);
    return new UnixClustering(supported);
  }

  public boolean isSupported() {
    return supported;
  }
}
