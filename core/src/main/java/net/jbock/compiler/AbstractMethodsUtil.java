package net.jbock.compiler;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AbstractMethodsUtil {

  private final Map<Name, List<ExecutableElement>> nonAbstractMethods;
  private final Types types;

  AbstractMethodsUtil(
      Map<Name, List<ExecutableElement>> nonAbstractMethods,
      Types types) {
    this.nonAbstractMethods = nonAbstractMethods;
    this.types = types;
  }

  /**
   * find abstract methods that are not implemented
   * further below in the hierarchy
   */
  List<ExecutableElement> findRelevantAbstractMethods(List<ExecutableElement> abstractMethods) {
    return abstractMethods.stream()
        .filter(this::isUnimplemented)
        .collect(Collectors.toList());
  }

  private boolean isUnimplemented(ExecutableElement abstractMethod) {
    Name name = abstractMethod.getSimpleName();
    List<ExecutableElement> methodsByTheSameName = nonAbstractMethods.get(name);
    if (methodsByTheSameName == null) {
      return true;
    }
    return methodsByTheSameName.stream()
        .noneMatch(method -> isSameSignature(method, abstractMethod));
  }

  private boolean isSameSignature(ExecutableElement m1, ExecutableElement m2) {
    if (m1.getParameters().size() != m2.getParameters().size()) {
      return false;
    }
    for (int i = 0; i < m1.getParameters().size(); i++) {
      VariableElement p1 = m1.getParameters().get(i);
      VariableElement p2 = m2.getParameters().get(i);
      if (!types.isSameType(p1.asType(), p2.asType())) {
        return false;
      }
    }
    return true;
  }
}
