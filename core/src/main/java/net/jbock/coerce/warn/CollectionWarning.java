package net.jbock.coerce.warn;

import net.jbock.compiler.Util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static net.jbock.compiler.InterfaceUtil.allInterfaces;
import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;

public class CollectionWarning extends Warning {

  @Override
  public String message(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return null;
    }
    DeclaredType declared = type.accept(Util.AS_DECLARED, null);
    TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    for (TypeMirror mirror : allInterfaces(typeElement.asType())) {
      if ("java.util.Collection".equals(mirror.accept(Util.QUALIFIED_NAME, null))) {
        return "This collection is not supported. Use List instead.";
      }
    }
    return null;
  }
}
