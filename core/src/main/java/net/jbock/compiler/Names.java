package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.LongName;
import net.jbock.ShortName;

import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

final class Names {

  final String shortName;
  final String longName;
  final boolean flag;

  private Names(String shortName, String longName, boolean flag) {
    this.shortName = shortName;
    this.longName = longName;
    this.flag = flag;
  }

  static boolean isFlag(VariableElement variableElement) {
    TypeName type = TypeName.get(variableElement.asType());
    return isFlag(type);
  }

  static boolean isFlag(TypeName type) {
    return type.equals(TypeName.BOOLEAN) ||
        type.equals(ClassName.get(Boolean.class));
  }

  static Names create(VariableElement variableElement) {
    LongName longName = variableElement.getAnnotation(LongName.class);
    ShortName shortName = variableElement.getAnnotation(ShortName.class);
    String ln = null, sn = null;
    TypeName type = TypeName.get(variableElement.asType());
    boolean flag = isFlag(type);
    if (flag) {
      if (shortName != null) {
        sn = shortName.value();
      }
      if (longName != null) {
        ln = longName.value();
      }
      if (shortName == null && longName == null) {
        sn = variableElement.getSimpleName().toString();
      }
    } else if (type.equals(Analyser.STRING)) {
      if (longName != null) {
        ln = longName.value();
      }
      if (shortName != null) {
        sn = shortName.value();
      }
      if (shortName == null && longName == null) {
        ln = variableElement.getSimpleName().toString();
      }
    } else {
      throw new ValidationException(Diagnostic.Kind.ERROR,
          "Only String or boolean allowed: " + variableElement.getSimpleName().toString(),
          variableElement);
    }
    checkName(variableElement, sn);
    checkName(variableElement, ln);
    return new Names(sn, ln, flag);
  }

  private static void checkName(VariableElement parameter, String name) {
    if (name == null) {
      return;
    }
    if (name.isEmpty()) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not be empty", parameter);
    }
    if (name.startsWith("-")) {
      throw new ValidationException(Diagnostic.Kind.ERROR, "The name may not start with '-'", parameter);
    }
  }
}
