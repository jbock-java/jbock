package net.jbock.coerce;

import net.jbock.compiler.HierarchyUtil;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.jbock.compiler.TypeTool.asDeclared;

class Resolver {

  /**
   * "Dog implements Animal"
   */
  private static class ImplementsRelation {

    final TypeElement dog;
    final DeclaredType animal;

    ImplementsRelation(TypeElement dog, DeclaredType animal) {
      this.dog = dog;
      this.animal = animal;
    }
  }

  private final List<ImplementsRelation> relations;
  private final TypeTool tool;

  private Resolver(List<ImplementsRelation> relations, TypeTool tool) {
    List<ImplementsRelation> reversed = new ArrayList<>(relations);
    Collections.reverse(reversed);
    this.relations = Collections.unmodifiableList(reversed);
    this.tool = tool;
  }

  /**
   * Check if {@code x} is a {@code something}, and also infer
   * the typevars in {@code something} where possible.
   *
   * @param x a type
   * @param something what we're hoping {@code x} is
   * @param tool a tool
   *
   * @return the {@code something} type, with typevars resolved
   */
  static Optional<TypeMirror> typecheck(TypeElement x, Class<?> something, TypeTool tool) {
    List<TypeElement> hierarchy = new HierarchyUtil(tool).getHierarchy(x);
    ImplementsRelation relation;
    TypeMirror nextGoal = tool.erasure(something);
    List<ImplementsRelation> implementsRelations = new ArrayList<>();
    while ((relation = findRelation(hierarchy, nextGoal, tool)) != null) {
      implementsRelations.add(relation);
      nextGoal = tool.erasure(relation.dog.asType());
    }
    return new Resolver(implementsRelations, tool).resolveTypevars();
  }

  private static ImplementsRelation findRelation(List<TypeElement> family, TypeMirror goal, TypeTool tool) {
    for (TypeElement element : family) {
      ImplementsRelation implementsRelation = findRelation(element, goal, tool);
      if (implementsRelation != null) {
        return implementsRelation;
      }
    }
    return null;
  }

  private static ImplementsRelation findRelation(TypeElement typeElement, TypeMirror goal, TypeTool tool) {
    for (TypeMirror mirror : typeElement.getInterfaces()) {
      if (tool.isSameType(goal, tool.erasure(mirror))) {
        return new ImplementsRelation(typeElement, asDeclared(mirror));
      }
    }
    return null;
  }

  private Optional<TypeMirror> resolveTypevars() {
    if (relations.isEmpty()) {
      return Optional.empty();
    }
    TypeMirror extensionClass = relations.get(0).animal;
    for (int i = 1; i < relations.size(); i++) {
      ImplementsRelation implementsRelation = relations.get(i);
      TypeMirror stepResult = resolveStep(extensionClass, implementsRelation);
      if (extensionClass == null) {
        return Optional.empty();
      }
      extensionClass = stepResult;
    }
    return Optional.of(extensionClass);
  }

  private TypeMirror resolveStep(TypeMirror x, ImplementsRelation implementsRelation) {
    List<? extends TypeMirror> typeArguments = asDeclared(x).getTypeArguments();
    List<? extends TypeParameterElement> typeParameters = implementsRelation.dog.getTypeParameters();
    Map<String, TypeMirror> solution = new HashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      solution.put(typeParameters.get(i).toString(), typeArguments.get(i));
    }
    DeclaredType input = implementsRelation.animal;
    return tool.substitute(input, solution);
  }

  @Override
  public String toString() {
    return relations.toString();
  }

}
