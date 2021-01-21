package net.jbock.coerce;

import javax.lang.model.type.TypeMirror;

public final class Util {

  public static String addBreaks(String code) {
    return code.replace(" ", "$W");
  }

  public static String noMatchError(TypeMirror type) {
    String typeName = type.toString();
    int i = typeName.lastIndexOf('.');
    if (i >= 0 && !typeName.matches(".*[<>].*")) { // TODO simple names
      typeName = typeName.substring(i + 1);
    }
    return "expecting mapper of type Function<String, " + typeName + ">";
  }
}
