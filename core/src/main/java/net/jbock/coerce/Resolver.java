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
   * Any free type parameters in Animal also appear in Dog.
   */
  static class ImplementsRelation {

    final TypeElement dog;
    final DeclaredType animal;

    ImplementsRelation(TypeElement dog, TypeMirror animal) {
      this.dog = dog;
      this.animal = asDeclared(animal);
    }
  }

  private final TypeTool tool;

  // visible for testing
  Resolver(TypeTool tool) {
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
    TypeMirror currentGoal = tool.erasure(something);
    List<ImplementsRelation> path = new ArrayList<>();
    Resolver resolver = new Resolver(tool);
    ImplementsRelation relation;
    while ((relation = resolver.findRelation(hierarchy, currentGoal)) != null) {
      path.add(relation);
      currentGoal = tool.erasure(relation.dog);
    }
    if (path.isEmpty()) {
      return Optional.empty();
    }
    Collections.reverse(path);
    return Optional.of(resolver.toAnimal(path));
  }

  private ImplementsRelation findRelation(List<TypeElement> hierarchy, TypeMirror something) {
    for (TypeElement type : hierarchy) {
      ImplementsRelation implementsRelation = findRelation(type, something);
      if (implementsRelation != null) {
        return implementsRelation;
      }
    }
    return null;
  }

  private ImplementsRelation findRelation(TypeElement type, TypeMirror something) {
    for (TypeMirror mirror : type.getInterfaces()) {
      if (tool.isSameType(something, tool.erasure(mirror))) {
        return new ImplementsRelation(type, mirror);
      }
    }
    return null;
  }

  /**
   * Successively replace type parameters.
   * The end result is a type that has the same erasure as the last segment's animal,
   * and uses the same type parameter names as the first path segment.
   *
   * @param path a path of implements relations
   * @return a type that has the same erasure as the final animal
   *
   * <ul>
   *   <li>ascending from dog to animal</li>
   *   <li>{@code path[1].dog} and {@code path[0].animal} have the same erasure</li>
   * </ul>
   */
  private TypeMirror toAnimal(List<ImplementsRelation> path) {
    TypeMirror acc = path.get(0).animal;
    for (int i = 1; i < path.size(); i++) {
      acc = toAnimal(acc, path.get(i));
    }
    return acc;
  }

  // visible for testing
  TypeMirror toAnimal(TypeMirror x, ImplementsRelation relation) {
    List<? extends TypeMirror> typeArguments = asDeclared(x).getTypeArguments();
    List<? extends TypeParameterElement> typeParameters = relation.dog.getTypeParameters();
    Map<String, TypeMirror> solution = new HashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      solution.put(typeParameters.get(i).toString(), typeArguments.get(i));
    }
    DeclaredType input = relation.animal;
    TypeMirror result = tool.substitute(input, solution);
    if (result == null) {
      throw new IllegalArgumentException();
    }
    return result;
  }
}
