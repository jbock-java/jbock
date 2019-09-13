package net.jbock.coerce.hint;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

class ArrayHint extends Hint {

  @Override
  String message(TypeElement type, boolean repeatable) {
    if (type.asType().getKind() == TypeKind.ARRAY) {
      if (!repeatable) {
        return "Declare this parameter repeatable.";
      } else {
        return "Use List, or define a custom collector.";
      }
    }
    return null;
  }
}
