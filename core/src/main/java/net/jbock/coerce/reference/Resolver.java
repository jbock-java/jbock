package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.either.Either;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static net.jbock.compiler.TypeTool.asDeclared;

class Resolver {

  private final ExpectedType<?> expectedType;

  private final BasicInfo basicInfo;

  // visible for testing
  Resolver(ExpectedType<?> expectedType, BasicInfo basicInfo) {
    this.expectedType = expectedType;
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
      return Optional.of(new Declared<>(something, x.getTypeArguments(), Collections.emptyList()));
    }
    List<ImplementsRelation> hierarchy = new HierarchyUtil(tool()).getHierarchy(tool().asTypeElement(x));
    List<ImplementsRelation> path = findPath(hierarchy, something);
    if (path.isEmpty()) {
      return Optional.empty();
    }
    DeclaredType declaredType = dogToAnimal(path);
    if (declaredType == null) {
      return Optional.empty();
    }
    return Optional.of(new Declared<>(something, declaredType.getTypeArguments(), path));
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
   * The end result is a type that has the same erasure as the last animal,
   * and uses the same type parameter names as the first dog.
   *
   * @param path a path of implements relations
   * @return a declared type that has the same erasure as the final animal
   *
   * <ul>
   *   <li>ascending from dog to animal</li>
   *   <li>{@code path[n].animal} has the same erasure as {@code path[n + 1].dog}</li>
   * </ul>
   */
  private DeclaredType dogToAnimal(List<ImplementsRelation> path) {
    List<Map<String, TypeMirror>> typevarMappings = new ArrayList<>();
    for (int i = 1; i < path.size(); i++) {
      typevarMappings.add(getTypevarMapping(path.get(i - 1).animal(), path.get(i).dog()));
    }
    Map<String, TypeMirror> typevarMapping = getMergedTypevarMapping(typevarMappings)
        .orElseThrow(basicInfo::asValidationException);
    DeclaredType animal = path.get(path.size() - 1).animal();
    return tool().substitute(animal, typevarMapping).orElseThrow(() ->
        basicInfo.asValidationException(expectedType.boom("substitution failed")));
  }

  private Either<String, Map<String, TypeMirror>> getMergedTypevarMapping(List<Map<String, TypeMirror>> solutions) {
    if (solutions.isEmpty()) {
      return Either.right(Collections.emptyMap());
    }
    Map<String, TypeMirror> solution = solutions.get(solutions.size() - 1);
    for (int i = solutions.size() - 2; i >= 0; i--) {
      Map<String, TypeMirror> merged = new LinkedHashMap<>();
      for (Entry<String, TypeMirror> entry : solution.entrySet()) {
        Optional<? extends TypeMirror> substituted = tool().substitute(entry.getValue(), solutions.get(i));
        if (!substituted.isPresent()) {
          return Either.left(expectedType.boom("substitution failed"));
        }
        merged.put(entry.getKey(), substituted.get());
      }
      solution = merged;
    }
    return Either.right(solution);
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
