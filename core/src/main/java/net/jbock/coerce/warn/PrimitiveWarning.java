package net.jbock.coerce.warn;

import javax.lang.model.type.TypeMirror;

public class PrimitiveWarning extends Warning {

  @Override
  public String message(TypeMirror type, boolean repeatable) {
    if (type.getKind().isPrimitive()) {
      return "This primitive is not supported.";
    }
    return null;
  }
}
