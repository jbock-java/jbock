package net.jbock.coerce.hint;

import net.jbock.compiler.HierarchyUtil;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

class OptionalHint extends Hint {

  private static final String NAME = Optional.class.getCanonicalName();

  @Override
  String message(TypeElement type, boolean repeatable) {
    for (TypeElement mirror : new HierarchyUtil(TypeTool.get()).getHierarchy(type)) {
      String qname = mirror.getQualifiedName().toString();
      if (NAME.equals(qname)) {
        return "Declare this parameter optional.";
      }
    }
    return null;
  }
}
