package net.jbock.compiler;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static net.jbock.compiler.Util.AS_DECLARED;
import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;

public class InterfaceUtil {

  public static List<TypeMirror> allInterfaces(TypeMirror mirror) {
    List<TypeMirror> acc = new ArrayList<>();
    accumulate(mirror, acc);
    return acc;
  }

  private static void accumulate(TypeMirror mirror, List<TypeMirror> acc) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return;
    }
    acc.add(mirror);
    DeclaredType d = mirror.accept(AS_DECLARED, null);
    TypeElement t = d.asElement().accept(AS_TYPE_ELEMENT, null);
    for (TypeMirror inter : t.getInterfaces()) {
      accumulate(inter, acc);
    }
    accumulate(t.getSuperclass(), acc);
  }
}
