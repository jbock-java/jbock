package net.jbock.coerce.reference;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class HierarchyUtil {

  private final TypeTool tool;

  HierarchyUtil(TypeTool tool) {
    this.tool = tool;
  }

  List<ImplementsRelation> findPath(TypeElement dog, Class<?> animal) {
    Optional<ImplementsRelation> relation = tool.getHierarchy(dog, animal);
    return relation.map(Collections::singletonList).orElse(Collections.emptyList());
  }
}
