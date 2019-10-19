package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static net.jbock.compiler.TypeTool.asDeclared;

class Resolver {

  private final BasicInfo basicInfo;

  // visible for testing
  Resolver(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  <E> Optional<Declared<E>> typecheck(TypeElement x, Class<E> something) {
    return typecheck(TypeTool.asDeclared(x.asType()), something);
  }

  /**
   * Check if {@code x} is a {@code something}, and also resolve
   * the typevars in {@code something} where possible.
   *
   * @param x a type
   * @param something what we're hoping {@code x} is an instance of
   *
   * @return A type that erases to the {@code something} type,
   * with typevars resolved where possible
   */
  <E> Optional<Declared<E>> typecheck(DeclaredType x, Class<E> something) {
    if (tool().isSameErasure(x, something)) {
      return Optional.of(new Declared<>(something, x.getTypeArguments(), Collections.emptyList(), Collections.emptyMap()));
    }
    List<ImplementsRelation> hierarchy = new HierarchyUtil(tool()).getHierarchy(tool().asTypeElement(x));
    List<ImplementsRelation> path = findPath(hierarchy, something);
    if (path.isEmpty()) {
      return Optional.empty();
    }
    Entry<DeclaredType, Map<String, TypeMirror>> result = dogToAnimal(path);
    DeclaredType declaredType = result.getKey();
    Map<String, TypeMirror> typevarMapping = result.getValue();
    return Optional.of(new Declared<>(something, declaredType.getTypeArguments(), path, typevarMapping));
  }

  private List<ImplementsRelation> findPath(List<ImplementsRelation> hierarchy, Class<?> something) {
    TypeMirror currentGoal = tool().erasure(something);
    List<ImplementsRelation> path = new ArrayList<>();
    ImplementsRelation relation;
    while ((relation = findRelation(hierarchy, currentGoal)) != null) {
      path.add(relation);
      currentGoal = tool().erasure(relation.dog());
    }
    Collections.reverse(path);
    return path;
  }

  private ImplementsRelation findRelation(List<ImplementsRelation> hierarchy, TypeMirror something) {
    for (ImplementsRelation relation : hierarchy) {
      if (tool().isSameType(something, tool().erasure(relation.animal()))) {
        return relation;
      }
    }
    return null;
  }

  /**
   * Go from dog to animal, successively replacing type parameters on the way.
   * The end result is a type that has the same erasure as the last segment's animal,
   * and uses the same type parameter names as the first segment.
   *
   * @param path a path of implements relations
   * @return a declared type that has the same erasure as the final animal
   *
   * <ul>
   *   <li>ascending from dog to animal</li>
   *   <li>{@code path[1].dog} and {@code path[0].animal} have the same erasure</li>
   * </ul>
   */
  private Entry<DeclaredType, Map<String, TypeMirror>> dogToAnimal(List<ImplementsRelation> path) {
    List<Map<String, TypeMirror>> typevarMappings = new ArrayList<>();
    for (int i = 1; i < path.size(); i++) {
      typevarMappings.add(getTypevarMapping(path.get(i - 1).animal(), path.get(i).dog()));
    }
    DeclaredType animal = path.get(path.size() - 1).animal();
    if (typevarMappings.isEmpty()) {
      return new SimpleImmutableEntry<>(animal, Collections.emptyMap());
    }
    Map<String, TypeMirror> typevarMapping = getMergedTypevarMapping(typevarMappings);
    return new SimpleImmutableEntry<>(tool().substitute(animal, typevarMapping), typevarMapping);
  }

  private Map<String, TypeMirror> getMergedTypevarMapping(List<Map<String, TypeMirror>> solutions) {
    Map<String, TypeMirror> solution = solutions.get(solutions.size() - 1);
    for (int i = solutions.size() - 2; i >= 0; i--) {
      Map<String, TypeMirror> merged = new LinkedHashMap<>();
      for (Entry<String, TypeMirror> entry : solution.entrySet()) {
        TypeMirror substituted = tool().substitute(entry.getValue(), solutions.get(i));
        merged.put(entry.getKey(), substituted);
      }
      solution = merged;
    }
    return solution;
  }

  private Map<String, TypeMirror> getTypevarMapping(DeclaredType animal, TypeElement dog) {
    List<? extends TypeMirror> typeArguments = asDeclared(animal).getTypeArguments();
    List<? extends TypeParameterElement> typeParameters = dog.getTypeParameters();
    if (typeArguments.size() != typeParameters.size()) {
      throw ValidationException.create(dog, "raw type");
    }
    Map<String, TypeMirror> solution = new LinkedHashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      TypeParameterElement param = typeParameters.get(i);
      TypeMirror arg = typeArguments.get(i);
      solution.put(param.toString(), arg);
    }
    return solution;
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
