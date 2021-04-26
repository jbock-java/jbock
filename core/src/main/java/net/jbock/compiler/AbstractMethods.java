package net.jbock.compiler;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AbstractMethods {

  final List<ExecutableElement> abstractMethods;
  final Map<Name, List<ExecutableElement>> nonabstractMethods;
  final Types types;

  private AbstractMethods(
      List<ExecutableElement> abstractMethods,
      Map<Name, List<ExecutableElement>> nonabstractMethods,
      Types types) {
    this.abstractMethods = abstractMethods;
    this.nonabstractMethods = nonabstractMethods;
    this.types = types;
  }

  static AbstractMethods create(
      List<ExecutableElement> abstractMethods,
      List<ExecutableElement> nonabstractMethods,
      Types types) {
    return new AbstractMethods(abstractMethods, nonabstractMethods.stream()
        .collect(Collectors.groupingBy(ExecutableElement::getSimpleName)), types);
  }

  List<ExecutableElement> unimplementedAbstract() {
    return abstractMethods.stream()
        .filter(abstractMethod -> nonabstractMethods.getOrDefault(abstractMethod.getSimpleName(), Collections.emptyList())
            .stream()
            .noneMatch(method -> isSameSignature(method, abstractMethod)))
        .collect(Collectors.toList());
  }

  private boolean isSameSignature(ExecutableElement method1, ExecutableElement method2) {
    if (method1.getParameters().size() != method2.getParameters().size()) {
      return false;
    }
    for (int i = 0; i < method1.getParameters().size(); i++) {
      VariableElement p1 = method1.getParameters().get(i);
      VariableElement p2 = method2.getParameters().get(i);
      if (!types.isSameType(p1.asType(), p2.asType())) {
        return false;
      }
    }
    return true;
  }
}
