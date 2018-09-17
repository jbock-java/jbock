package net.jbock.compiler;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.jbock.compiler.Util.AS_DECLARED;
import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;
import static net.jbock.compiler.Util.asDeclared;

public class HierarchyUtil {

  public static List<TypeElement> getFamilyTree(TypeMirror mirror) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return Collections.emptyList();
    }
    List<TypeElement> acc = new ArrayList<>();
    accumulate(mirror, acc);
    return acc;
  }

  private static void accumulate(TypeMirror mirror, List<TypeElement> acc) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return;
    }
    TypeElement t = asTypeElement(mirror);
    acc.add(t);
    for (TypeMirror inter : t.getInterfaces()) {
      accumulate(inter, acc);
    }
    accumulate(t.getSuperclass(), acc);
  }

  public static TypeElement asTypeElement(TypeMirror mirror) {
    DeclaredType declared = mirror.accept(Util.AS_DECLARED, null);
    return declared.asElement().accept(Util.AS_TYPE_ELEMENT, null);
  }
}
