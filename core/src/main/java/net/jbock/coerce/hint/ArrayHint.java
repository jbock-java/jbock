package net.jbock.coerce.hint;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class ArrayHint extends Hint {

  @Override
  public String message(TypeMirror type, boolean repeatable) {
    if (type.getKind() == TypeKind.ARRAY) {
      if (!repeatable) {
        return "Declare this parameter repeatable.";
      } else {
        return "Use List, or define a custom collector.";
      }
    }
    return null;
  }
}
