package net.jbock.compiler;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.qualifier.SourceMethod;

import javax.lang.model.element.ExecutableElement;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class Methods {

  private static final Comparator<ExecutableElement> POSITION_COMPARATOR =
      (m1, m2) -> {
        Parameter param1 = m1.getAnnotation(Parameter.class);
        Parameter param2 = m2.getAnnotation(Parameter.class);
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

  static Methods create(List<ExecutableElement> methods) {
    List<SourceMethod> params = methods.stream()
        .filter(m -> m.getAnnotation(Parameter.class) != null ||
            m.getAnnotation(Parameters.class) != null)
        .sorted(POSITION_COMPARATOR)
        .map(SourceMethod::create)
        .collect(Collectors.toList());
    List<SourceMethod> options = methods.stream()
        .filter(m -> m.getAnnotation(Option.class) != null)
        .map(SourceMethod::create)
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
