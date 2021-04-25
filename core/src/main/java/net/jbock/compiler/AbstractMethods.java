package net.jbock.compiler;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class AbstractMethods {

  final List<ExecutableElement> abstractMethods;
  final Map<Name, List<ExecutableElement>> nonabstractMethods;

  private AbstractMethods(List<ExecutableElement> abstractMethods, Map<Name, List<ExecutableElement>> nonabstractMethods) {
    this.abstractMethods = abstractMethods;
    this.nonabstractMethods = nonabstractMethods;
  }

  static AbstractMethods create(List<ExecutableElement> abstractMethods, List<ExecutableElement> nonabstractMethods) {
    return new AbstractMethods(abstractMethods, nonabstractMethods.stream()
        .collect(Collectors.groupingBy(ExecutableElement::getSimpleName)));
  }

  List<ExecutableElement> unimplementedAbstract() {
    return abstractMethods.stream()
        .filter(m -> {
          List<ExecutableElement> methods = nonabstractMethods.getOrDefault(m.getSimpleName(), Collections.emptyList());
          for (ExecutableElement method : methods) {
            if (m.getParameters().size() == method.getParameters().size()) {
              // TODO better check if signature are equal
              return false;
            }
          }
          return true;
        })
        .collect(Collectors.toList());
  }
}
