package net.jbock.compiler;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HierarchyUtil {

  public static List<TypeElement> getTypeTree(
      TypeMirror mirror,
      TypeTool tool) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return Collections.emptyList();
    }
    List<TypeElement> acc = new ArrayList<>();
    accumulate(mirror, acc, tool);
    return acc;
  }

  private static void accumulate(
      TypeMirror mirror,
      List<TypeElement> acc,
      TypeTool tool) {
    if (mirror == null || mirror.getKind() != TypeKind.DECLARED) {
      return;
    }
    TypeElement t = tool.asTypeElement(mirror);
    acc.add(t);
    for (TypeMirror inter : t.getInterfaces()) {
      accumulate(inter, acc, tool);
    }
    accumulate(t.getSuperclass(), acc, tool);
  }
}
