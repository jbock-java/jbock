package net.jbock.coerce.hint;

import javax.lang.model.type.TypeMirror;

public class PrimitiveHint extends Hint {

  @Override
  public String message(TypeMirror type, boolean repeatable) {
    if (type.getKind().isPrimitive()) {
      return "This primitive is not supported.";
    }
    return null;
  }
}
