package net.jbock.coerce.reference;

import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.TypevarMapping;
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
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;
import static net.jbock.coerce.reference.TypecheckFailure.fatal;
import static net.jbock.coerce.reference.TypecheckFailure.nonFatal;
import static net.jbock.compiler.TypeTool.asDeclared;

class Resolver {

  private final ExpectedType<?> expectedType;

  private final TypeTool tool;

  // visible for testing
  Resolver(ExpectedType<?> expectedType, TypeTool tool) {
    this.expectedType = expectedType;
    this.tool = tool;
  }

  /**
   * Check if {@code dog} is a {@code animal}, and also resolve
   * the typevars in {@code animal} where possible.
   *
   * @param dog a type
   * @param animal an interface or abstract class
   *
   * @return A type that has the same erasure as {@code animal}
   */
  <E> Either<TypecheckFailure, Declared<E>> typecheck(TypeElement dog, Class<E> animal) {
    List<ImplementsRelation> hierarchy = new HierarchyUtil(tool).getHierarchy(dog);
    List<ImplementsRelation> path = findPath(hierarchy, animal);
    if (path.isEmpty()) {
      return left(nonFatal("not a " + animal.getCanonicalName()));
    }
    assert path.get(0).dog() == dog;
    if (tool.isRaw(path.get(path.size() - 1).animal())) {
      return left(fatal("raw type: " + path.get(path.size() - 1).animal()));
    }
    return dogToAnimal(path).map(Function.identity(),
        declaredType -> new Declared<>(animal, declaredType.getTypeArguments()));
  }

  public <E> Either<TypecheckFailure, Declared<E>> typecheck(DeclaredType declared, Class<E> someInterface) {
    if (!tool.isSameErasure(declared, someInterface)) {
      return left(nonFatal("not a declared " + someInterface.getCanonicalName()));
    }
    if (tool.isRaw(declared)) {
      return left(fatal("raw type: " + declared));
    }
    return right(new Declared<>(someInterface, declared.getTypeArguments()));
  }

  private List<ImplementsRelation> findPath(List<ImplementsRelation> hierarchy, Class<?> goal) {
    TypeMirror currentGoal = tool.asType(goal);
    List<ImplementsRelation> path = new ArrayList<>();
    ImplementsRelation relation;
    while ((relation = findRelation(hierarchy, currentGoal)) != null) {
      path.add(relation);
      currentGoal = relation.dog().asType();
    }
    Collections.reverse(path);
    return path;
  }

  private ImplementsRelation findRelation(List<ImplementsRelation> hierarchy, TypeMirror goal) {
    for (ImplementsRelation relation : hierarchy) {
      if (tool.isSameErasure(goal, relation.animal())) {
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
  private Either<TypecheckFailure, DeclaredType> dogToAnimal(List<ImplementsRelation> path) {
    List<TypevarMapping> typevarMappings = new ArrayList<>();
    for (int i = 1; i < path.size(); i++) {
      typevarMappings.add(getTypevarMapping(path.get(i - 1).animal(), path.get(i).dog()));
    }
    return getMergedTypevarMapping(typevarMappings)
        .flatMap(Function.identity(), solution -> {
          DeclaredType animal = path.get(path.size() - 1).animal();
          return solution.substitute(animal);
        });
  }

  private Either<TypecheckFailure, TypevarMapping> getMergedTypevarMapping(List<TypevarMapping> solutions) {
    if (solutions.isEmpty()) {
      return right(new TypevarMapping(Collections.emptyMap(), tool));
    }
    TypevarMapping solution = solutions.get(solutions.size() - 1);
    for (int i = solutions.size() - 2; i >= 0; i--) {
      Map<String, TypeMirror> merged = new LinkedHashMap<>();
      for (Entry<String, TypeMirror> entry : solution.entries()) {
        Either<TypecheckFailure, TypeMirror> substituted = solutions.get(i).substitute(entry.getValue());
        if (substituted instanceof Left) {
          return left(fatal(expectedType.boom(((Left<TypecheckFailure, TypeMirror>) substituted).value().getMessage())));
        }
        merged.put(entry.getKey(), ((Right<TypecheckFailure, TypeMirror>) substituted).value());
      }
      solution = new TypevarMapping(merged, tool);
    }
    return right(solution);
  }

  private TypevarMapping getTypevarMapping(DeclaredType animal, TypeElement dog) {
    List<? extends TypeMirror> typeArguments = asDeclared(animal).getTypeArguments();
    List<? extends TypeParameterElement> typeParameters = dog.getTypeParameters();
    if (typeArguments.size() != typeParameters.size()) {
      throw ValidationException.create(dog, "raw type: " + animal);
    }
    Map<String, TypeMirror> solution = new LinkedHashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      TypeParameterElement param = typeParameters.get(i);
      TypeMirror arg = typeArguments.get(i);
      solution.put(param.toString(), arg);
    }
    return new TypevarMapping(solution, tool);
  }
}
