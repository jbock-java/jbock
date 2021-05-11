package net.jbock.compiler;

import net.jbock.Parameter;
import net.jbock.qualifier.SourceMethod;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class Methods {

  private static final Comparator<SourceMethod> POSITION_COMPARATOR =
      (m1, m2) -> {
        Parameter param1 = m1.method().getAnnotation(Parameter.class);
        Parameter param2 = m2.method().getAnnotation(Parameter.class);
        boolean p1 = param1 != null;
        boolean p2 = param2 != null;
        if (p1 && !p2) {
          return -1;
        }
        if (!p1 && p2) {
          return 1;
        }
        if (!p1) {
          return 0; // both are null, should be impossible but let's get rid of the warning
        }
        return Integer.compare(param1.index(), param2.index());
      };

  private final List<SourceMethod> params;
  private final List<SourceMethod> options;

  private Methods(List<SourceMethod> params, List<SourceMethod> options) {
    this.params = params;
    this.options = options;
  }

  static Methods create(List<SourceMethod> methods) {
    List<SourceMethod> params = methods.stream()
        .filter(m -> m.style().isPositional())
        .sorted(POSITION_COMPARATOR)
        .collect(Collectors.toList());
    List<SourceMethod> options = methods.stream()
        .filter(m -> !m.style().isPositional())
        .collect(Collectors.toList());
    return new Methods(params, options);
  }

  List<SourceMethod> params() {
    return params;
  }

  List<SourceMethod> options() {
    return options;
  }
}
