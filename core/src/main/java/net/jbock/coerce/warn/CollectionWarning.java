package net.jbock.coerce.warn;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.HierarchyUtil.getFamilyTree;

public class CollectionWarning extends Warning {

  @Override
  public String message(TypeMirror type) {
    for (TypeElement mirror : getFamilyTree(type)) {
      if ("java.util.Collection".equals(mirror.getQualifiedName().toString())) {
        return "This collection is not supported. Use List instead.";
      }
    }
    return null;
  }
}
