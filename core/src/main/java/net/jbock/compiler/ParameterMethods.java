package net.jbock.compiler;

import net.jbock.PositionalParameter;

import javax.lang.model.element.ExecutableElement;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.partitioningBy;

class ParameterMethods {

  private static final Comparator<ExecutableElement> POSITION_COMPARATOR = Comparator
      .comparingInt(e -> e.getAnnotation(PositionalParameter.class).position());

  private final List<ExecutableElement> positional;
  private final List<ExecutableElement> options;

  private ParameterMethods(List<ExecutableElement> positional, List<ExecutableElement> options) {
    this.positional = positional;
    this.options = options;
  }

  static ParameterMethods create(List<ExecutableElement> abstractMethods) {
    Map<Boolean, List<ExecutableElement>> partition = abstractMethods.stream()
        .collect(partitioningBy(method -> method.getAnnotation(PositionalParameter.class) != null));
    List<ExecutableElement> positional = partition.getOrDefault(true, emptyList());
    checkPositionUnique(positional);
    List<ExecutableElement> sortedPositional = positional.stream()
        .sorted(POSITION_COMPARATOR)
        .collect(Collectors.toList());
    return new ParameterMethods(sortedPositional, partition.getOrDefault(false, emptyList()));
  }

  private static void checkPositionUnique(List<ExecutableElement> allPositional) {
    Set<Integer> positions = new HashSet<>();
    for (ExecutableElement method : allPositional) {
      Integer position = method.getAnnotation(PositionalParameter.class).position();
      if (!positions.add(position)) {
        throw ValidationException.create(method, "Define a unique position.");
      }
    }
  }


  List<ExecutableElement> positionals() {
    return positional;
  }

  List<ExecutableElement> options() {
    return options;
  }
}
