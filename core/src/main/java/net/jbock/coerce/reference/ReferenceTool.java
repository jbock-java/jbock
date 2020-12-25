package net.jbock.coerce.reference;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.jbock.compiler.TypeTool.asDeclared;

public class ReferenceTool<E> {

  private final Resolver resolver;
  private final TypeTool tool;

  private final Function<String, ValidationException> errorHandler;
  private final TypeElement referencedClass;
  private final ExpectedType<E> expectedType;

  public ReferenceTool(ExpectedType<E> expectedType, Function<String, ValidationException> errorHandler, TypeTool tool, TypeElement referencedClass) {
    this.expectedType = expectedType;
    this.errorHandler = errorHandler;
    this.referencedClass = referencedClass;
    this.resolver = new Resolver(tool, this::boom);
    this.tool = tool;
  }

  public ReferencedType<E> getReferencedType() {
    return resolver.checkImplements(referencedClass, Supplier.class)
        .map(this::handleSupplier)
        .orElseGet(this::handleNotSupplier);
  }

  private ReferencedType<E> handleNotSupplier() {
    List<? extends TypeMirror> expected = resolver.checkImplements(referencedClass, expectedType.expectedClass())
        .orElseThrow(() -> boom("not a " + expectedType.expectedClass().getSimpleName() +
            " or Supplier<" + expectedType.expectedClass().getSimpleName() + ">"));
    return new ReferencedType<>(expected, false);
  }

  private ReferencedType<E> handleSupplier(List<? extends TypeMirror> typeArguments) {
    TypeMirror supplied = typeArguments.get(0);
    if (supplied.getKind() != TypeKind.DECLARED) {
      throw boom("not a " + expectedType.simpleName() + " or Supplier<" + expectedType.simpleName() + ">");
    }
    DeclaredType actual = asDeclared(supplied);
    Class<E> expected = expectedType.expectedClass();
    if (!tool.isSameErasure(actual, expected)) {
      throw boom("expected " + expected.getCanonicalName() + " but found " + actual);
    }
    if (tool.isRaw(actual)) {
      throw boom("raw type: " + actual);
    }
    return new ReferencedType<>(actual.getTypeArguments(), true);
  }

  private ValidationException boom(String message) {
    return errorHandler.apply(expectedType.boom(message));
  }
}
