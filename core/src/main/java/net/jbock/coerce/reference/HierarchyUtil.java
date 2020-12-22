package net.jbock.coerce.reference;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class HierarchyUtil {

  private final TypeTool tool;

  HierarchyUtil(TypeTool tool) {
    this.tool = tool;
  }

  List<ImplementsRelation> findPath(TypeElement dog, Class<?> animal) {
    List<ImplementsRelation> hierarchy = getHierarchy(dog);
    TypeMirror currentAnimal = tool.asType(animal);
    List<ImplementsRelation> path = new ArrayList<>();
    ImplementsRelation relation;
    while ((relation = findRelation(hierarchy, currentAnimal)) != null) {
      path.add(relation);
      currentAnimal = relation.dog().asType();
    }
    Collections.reverse(path);
    return path;
  }

  private List<ImplementsRelation> getHierarchy(TypeElement dog) {
    List<ImplementsRelation> acc = new ArrayList<>();
    accumulate(dog.asType(), acc);
    return acc;
  }

  private void accumulate(TypeMirror dog, List<ImplementsRelation> acc) {
    if (dog == null || dog.getKind() != TypeKind.DECLARED) {
      return;
    }
    TypeElement t = tool.asTypeElement(dog);
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

  private ImplementsRelation findRelation(List<ImplementsRelation> hierarchy, TypeMirror animal) {
    for (ImplementsRelation relation : hierarchy) {
      if (tool.isSameErasure(animal, relation.animal())) {
        return relation;
      }
    }
    return null;
  }
}
