package net.jbock.coerce.warn;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class ArrayWarning extends Warning {

  @Override
  public String message(TypeMirror type) {
    if (type.getKind() == TypeKind.ARRAY) {
      return "Arrays are not supported. Use java.util.List instead.";
    }
    return null;
  }
}
