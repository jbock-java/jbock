package net.jbock.coerce.reference;

import net.jbock.coerce.either.Either;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;
import static net.jbock.coerce.reference.TypecheckFailure.typeFail;
import static net.jbock.compiler.TypeTool.asDeclared;

class Resolver {

  private final TypeTool tool;
  private final Function<String, ValidationException> errorHandler;

  // visible for testing
  Resolver(TypeTool tool, Function<String, ValidationException> errorHandler) {
    this.tool = tool;
    this.errorHandler = errorHandler;
  }

  /**
   * Check if {@code dog} implements {@code animal}.
   *
   * @param dog a type
   * @param animal an interface or abstract class
   *
   * @return the list of typeargs that are passed to {@code animal}
   * in the declaration of {@code dog}
   */
  <E> Either<TypecheckFailure, List<? extends TypeMirror>> checkImplements(TypeElement dog, Class<E> animal) {
    Optional<DeclaredType> opt = tool.checkImplements(dog, animal);
    if (!opt.isPresent()) {
      return left(typeFail("not a " + animal.getCanonicalName()));
    }
    DeclaredType declared = opt.get();
    List<? extends TypeMirror> typeArguments = asDeclared(declared).getTypeArguments();
    List<? extends TypeParameterElement> typeParams = tool.asTypeElement(tool.asType(animal)).getTypeParameters();
    if (typeArguments.size() != typeParams.size()) {
      throw errorHandler.apply("raw type: " + declared);
    }
    return right(declared.getTypeArguments());
  }
}
