package net.jbock.compiler;

import net.jbock.Option;
import net.jbock.Param;

import javax.lang.model.element.ExecutableElement;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class Methods {

  private static final Comparator<ExecutableElement> POSITION_COMPARATOR = Comparator
      .comparingInt(e -> e.getAnnotation(Param.class).value());

  private final List<ExecutableElement> params;
  private final List<ExecutableElement> options;

  private Methods(List<ExecutableElement> params, List<ExecutableElement> options) {
    this.params = params;
    this.options = options;
  }

  static Methods create(List<ExecutableElement> methods) {
    List<ExecutableElement> params = methods.stream()
        .filter(m -> m.getAnnotation(Param.class) != null)
        .sorted(POSITION_COMPARATOR)
        .collect(Collectors.toList());
    List<ExecutableElement> options = methods.stream()
        .filter(m -> m.getAnnotation(Option.class) != null)
        .collect(Collectors.toList());
    return new Methods(params, options);
  }

  List<ExecutableElement> params() {
    return params;
  }

  List<ExecutableElement> options() {
    return options;
  }
}
