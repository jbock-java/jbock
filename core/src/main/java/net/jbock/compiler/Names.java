package net.jbock.compiler;

import net.jbock.Flag;
import net.jbock.LongName;
import net.jbock.ShortName;

import javax.lang.model.element.VariableElement;

final class Names {

  final String shortName;
  final String longName;
  final boolean flag;

  private Names(String shortName, String longName, boolean flag) {
    this.shortName = shortName;
    this.longName = longName;
    this.flag = flag;
  }

  static Names create(VariableElement variableElement) {
    LongName longName = variableElement.getAnnotation(LongName.class);
    ShortName shortName = variableElement.getAnnotation(ShortName.class);
    String ln = null;
    String sn = null;
    boolean flag = false;
    if (variableElement.getAnnotation(Flag.class) != null) {
      flag = true;
      if (shortName != null) {
        sn = shortName.value();
      }
      if (longName != null) {
        ln = longName.value();
      }
      if (shortName == null && longName == null) {
        sn = variableElement.getSimpleName().toString();
      }
    } else {
      if (longName != null) {
        ln = longName.value();
      }
      if (shortName != null) {
        sn = shortName.value();
      }
      if (shortName == null && longName == null) {
        ln = variableElement.getSimpleName().toString();
      }
    }
    return new Names(sn, ln, flag);
  }
}
