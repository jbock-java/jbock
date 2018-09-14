package net.jbock.coerce.warn;

import net.jbock.compiler.Util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static net.jbock.compiler.Util.AS_DECLARED;
import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;

public class CollectionWarning extends Warning {

  @Override
  public String message(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return null;
    }
    DeclaredType declared = type.accept(Util.AS_DECLARED, null);
    TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    for (String qname : allInterfaces(typeElement.asType())) {
      if ("java.util.Collection".equals(qname)) {
        return "This collection is not supported. Use List instead.";
      }
    }
    return null;
  }

  private static List<String> allInterfaces(TypeMirror typeSuperclass) {
    List<String> acc = new ArrayList<>();
    accumulate(typeSuperclass, acc);
    return acc;
  }

  private static void accumulate(TypeMirror mirror, List<String> acc) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return;
    }
    acc.add(mirror.accept(Util.QUALIFIED_NAME, null));
    DeclaredType d = mirror.accept(AS_DECLARED, null);
    TypeElement t = d.asElement().accept(AS_TYPE_ELEMENT, null);
    for (TypeMirror inter : t.getInterfaces()) {
      accumulate(inter, acc);
    }
    accumulate(t.getSuperclass(), acc);
  }
}
