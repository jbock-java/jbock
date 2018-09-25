package net.jbock.coerce.warn;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.HierarchyUtil.getTypeTree;

public class CollectionWarning extends Warning {

  @Override
  public String message(TypeMirror type) {
    for (TypeElement mirror : getTypeTree(type)) {
      String qname = mirror.getQualifiedName().toString();
      if ("java.util.List".equals(qname)) {
        return "Either declare this argument repeatable, or use a custom mapper.";
      }
      if ("java.util.Collection".equals(qname)) {
        return "This collection is not supported. Use List instead.";
      }
    }
    return null;
  }
}
