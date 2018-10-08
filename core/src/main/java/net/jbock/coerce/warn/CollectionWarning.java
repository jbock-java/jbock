package net.jbock.coerce.warn;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Set;
import java.util.stream.Collectors;

import static net.jbock.compiler.HierarchyUtil.getTypeTree;

public class CollectionWarning extends Warning {

  @Override
  public String message(TypeMirror type, boolean repeatable) {
    Set<String> names = getTypeTree(type).stream()
        .map(TypeElement::getQualifiedName)
        .map(Name::toString)
        .collect(Collectors.toSet());
    if (names.contains("java.util.List")) {
      if (!repeatable) {
        return "Declare this parameter repeatable.";
      } else {
        return "Define a custom mapper.";
      }
    }
    if (names.contains("java.util.Collection") || names.contains("java.util.Map")) {
      if (!repeatable) {
        return "Declare this parameter repeatable.";
      } else {
        return "Define a custom collector. Alternatively, use List instead.";
      }
    }
    return null;
  }
}
