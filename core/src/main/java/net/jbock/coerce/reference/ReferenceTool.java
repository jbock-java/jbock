package net.jbock.coerce.reference;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.compiler.TypeTool.asDeclared;

public class ReferenceTool<E> {

  private final TypeTool tool;
  private final Function<String, ValidationException> errorHandler;
  private final TypeElement referencedClass;
  private final ExpectedType<E> expectedType;

  public ReferenceTool(
      ExpectedType<E> expectedType,
      Function<String, ValidationException> errorHandler,
      TypeTool tool,
      TypeElement referencedClass) {
    this.expectedType = expectedType;
    this.errorHandler = errorHandler;
    this.referencedClass = referencedClass;
    this.tool = tool;
  }

  public ReferencedType<E> getReferencedType() {
    return checkImplements(referencedClass, Supplier.class.getCanonicalName())
        .map(this::handleSupplier)
        .orElseGet(this::handleNotSupplier);
  }

  private ReferencedType<E> handleNotSupplier() {
    List<? extends TypeMirror> expected = checkImplements(referencedClass, expectedType.canonicalName())
        .orElseThrow(() -> boom("not a " + expectedType.canonicalName() +
            " or " + Supplier.class.getCanonicalName() +
            "<" + expectedType.canonicalName() + ">"));
    return new ReferencedType<>(expected, false);
  }

  private ReferencedType<E> handleSupplier(List<? extends TypeMirror> typeArguments) {
    TypeMirror supplied = typeArguments.get(0);
    if (supplied.getKind() != TypeKind.DECLARED) {
      throw boom("not a " + expectedType.canonicalName() + " or " + Supplier.class.getCanonicalName() +
          "<" + expectedType.canonicalName() + ">");
    }
    DeclaredType actual = asDeclared(supplied);
    if (!tool.isSameErasure(actual, expectedType.canonicalName())) {
      throw boom("expected " + expectedType.canonicalName() + " but found " + actual);
    }
    if (tool.isRaw(actual)) {
      throw boom("raw type: " + actual);
    }
    return new ReferencedType<>(actual.getTypeArguments(), true);
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
  private Optional<List<? extends TypeMirror>> checkImplements(TypeElement dog, String animal) {
    return tool.checkImplements(dog, animal)
        .map(declared -> {
          List<? extends TypeMirror> typeArguments = declared.getTypeArguments();
          List<? extends TypeParameterElement> typeParams = tool.asTypeElement(tool.asType(animal)).getTypeParameters();
          if (typeArguments.size() != typeParams.size()) {
            throw errorHandler.apply("raw type: " + declared);
          }
          return declared.getTypeArguments();
        });
  }

  private ValidationException boom(String message) {
    return errorHandler.apply(message);
  }
}
