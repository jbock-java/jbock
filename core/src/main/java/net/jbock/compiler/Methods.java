package net.jbock.compiler;

import net.jbock.qualifier.SourceMethod;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Methods {

  private static final Comparator<SourceMethod> POSITION_COMPARATOR =
      Comparator.comparingInt(m -> m.index().orElse(Integer.MAX_VALUE));

  private final List<SourceMethod> params;
  private final List<SourceMethod> options;

  private Methods(List<SourceMethod> params, List<SourceMethod> options) {
    this.params = params;
    this.options = options;
  }

  public static Methods create(List<SourceMethod> methods) {
    List<SourceMethod> params = methods.stream()
        .filter(m -> m.style().isPositional())
        .sorted(POSITION_COMPARATOR)
        .collect(Collectors.toList());
    List<SourceMethod> options = methods.stream()
        .filter(m -> !m.style().isPositional())
        .collect(Collectors.toList());
    return new Methods(params, options);
  }

  public List<SourceMethod> params() {
    return params;
  }

  public List<SourceMethod> options() {
    return options;
  }
}
