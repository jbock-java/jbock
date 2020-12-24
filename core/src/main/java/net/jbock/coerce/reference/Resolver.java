package net.jbock.coerce.reference;

import net.jbock.coerce.either.Either;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;
import static net.jbock.coerce.reference.TypecheckFailure.fatal;
import static net.jbock.coerce.reference.TypecheckFailure.nonFatal;
import static net.jbock.compiler.TypeTool.asDeclared;

class Resolver {

  private final TypeTool tool;

  // visible for testing
  Resolver(TypeTool tool) {
    this.tool = tool;
  }

  /**
   * Check if {@code dog} is a {@code animal}, and also resolve
   * the typevars in {@code animal} where possible.
   *
   * @param dog a type
   * @param animal an interface or abstract class
   *
   * @return the list of typeargs that are passed to {@code animal}
   * in the declaration of {@code dog}
   */
  <E> Either<TypecheckFailure, List<? extends TypeMirror>> typecheck(TypeElement dog, Class<E> animal) {
    Optional<DeclaredType> opt = tool.checkImplements(dog, animal);
    if (!opt.isPresent()) {
      return left(nonFatal("not a " + animal.getCanonicalName()));
    }
    DeclaredType declared = opt.get();
    List<? extends TypeMirror> typeArguments = asDeclared(declared).getTypeArguments();
    List<? extends TypeParameterElement> typeParams = tool.asTypeElement(tool.asType(animal)).getTypeParameters();
    if (typeArguments.size() != typeParams.size()) {
      return left(fatal("raw type: " + declared));
    }
    return right(declared.getTypeArguments());
  }

  // TODO javadoc, and possibly rename this method as it seems to do domething different
  public <E> Either<TypecheckFailure, List<? extends TypeMirror>> typecheck(DeclaredType declared, Class<E> someInterface) {
    if (!tool.isSameErasure(declared, someInterface)) {
      return left(nonFatal("expected " + someInterface.getCanonicalName() + " but found " + declared));
    }
    if (tool.isRaw(declared)) {
      return left(fatal("raw type: " + declared));
    }
    return right(declared.getTypeArguments());
  }
}
