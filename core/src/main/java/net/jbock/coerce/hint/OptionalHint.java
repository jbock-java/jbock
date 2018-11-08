package net.jbock.coerce.hint;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.compiler.HierarchyUtil.getTypeTree;

public class OptionalHint extends Hint {

  private static final String NAME = Optional.class.getCanonicalName();

  @Override
  public String message(TypeMirror type, boolean repeatable) {
    for (TypeElement mirror : getTypeTree(type)) {
      String qname = mirror.getQualifiedName().toString();
      if (NAME.equals(qname)) {
        return "Declare this parameter optional.";
      }
    }
    return null;
  }
}
