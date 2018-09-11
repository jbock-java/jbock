package net.jbock.coerce.warn;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static net.jbock.compiler.Util.AS_TYPE_ELEMENT;
import static net.jbock.compiler.Util.asDeclared;

public class CollectionWarning extends Warning {

  @Override
  public String message(TypeMirror type) {
    if (type.getKind().isPrimitive()) {
      return null;
    }
    DeclaredType declared = asDeclared(type);
    if (declared == null) {
      return null;
    }
    TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    if (typeElement == null) {
      return null;
    }
    for (String qname : allInterfaces(type)) {
      if ("java.util.Collection".equals(qname)) {
        return "This collection is not supported. Use List instead.";
      }
    }
    return null;
  }

  private static List<String> allInterfaces(TypeMirror typeSuperclass) {
    List<String> acc = new ArrayList<>();
    allInterfaces(typeSuperclass, acc);
    return acc;
  }

  private static void allInterfaces(TypeMirror mirror, List<String> acc) {
    if (mirror == null) {
      return;
    }
    String qname = getQualifiedName(mirror);
    if (qname != null) {
      acc.add(qname);
    }
    DeclaredType d = asDeclared(mirror);
    if (d == null) {
      return;
    }
    TypeElement t = d.asElement().accept(AS_TYPE_ELEMENT, null);
    if (t == null) {
      return;
    }
    for (TypeMirror inter : t.getInterfaces()) {
      allInterfaces(inter, acc);
    }
    allInterfaces(t.getSuperclass(), acc);
  }

  private static String getQualifiedName(TypeMirror mirror) {
    if (mirror == null) {
      return null;
    }
    DeclaredType declared = asDeclared(mirror);
    if (declared == null) {
      return null;
    }
    TypeElement typeElement = declared.asElement().accept(AS_TYPE_ELEMENT, null);
    if (typeElement == null) {
      return null;
    }
    return typeElement.getQualifiedName().toString();
  }
}
