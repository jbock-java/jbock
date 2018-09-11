package net.jbock.coerce.warn;

import javax.lang.model.type.TypeMirror;

public class PrimitiveWarning extends Warning {

  @Override
  public String message(TypeMirror type) {
    if (type.getKind().isPrimitive()) {
      return "This primitive is not supported. Try int, long, float, double, char or boolean.";
    }
    return null;
  }
}
