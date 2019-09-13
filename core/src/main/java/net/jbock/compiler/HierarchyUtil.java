package net.jbock.compiler;

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

  public List<TypeElement> getHierarchy(TypeElement typeElement) {
    List<TypeElement> acc = new ArrayList<>();
    accumulate(typeElement.asType(), acc);
    return acc;
  }

  private void accumulate(TypeMirror mirror, List<TypeElement> acc) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return;
    }
    TypeElement t = tool.asTypeElement(mirror);
    acc.add(t);
    for (TypeMirror inter : t.getInterfaces()) {
      accumulate(inter, acc);
    }
    accumulate(t.getSuperclass(), acc);
  }
}
