package net.jbock.compiler;

import net.jbock.coerce.ImplementsRelation;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

public class HierarchyUtil {

  private final TypeTool tool;

  public HierarchyUtil(TypeTool tool) {
    this.tool = tool;
  }

  public List<ImplementsRelation> getHierarchy(TypeElement typeElement) {
    List<ImplementsRelation> acc = new ArrayList<>();
    accumulate(typeElement.asType(), acc);
    return acc;
  }

  private void accumulate(TypeMirror mirror, List<ImplementsRelation> acc) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return;
    }
    TypeElement t = tool.asTypeElement(mirror);
    for (TypeMirror inter : t.getInterfaces()) {
      acc.add(new ImplementsRelation(t, inter));
      accumulate(inter, acc);
    }
    TypeMirror superclass = t.getSuperclass();
    if (superclass != null && superclass.getKind() == TypeKind.DECLARED) {
      acc.add(new ImplementsRelation(t, superclass));
      accumulate(superclass, acc);
    }
  }
}
