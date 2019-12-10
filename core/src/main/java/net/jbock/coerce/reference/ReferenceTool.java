package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Locale;
import java.util.function.Supplier;

import static net.jbock.compiler.TypeTool.asDeclared;

public class ReferenceTool<E> {

  private final Resolver resolver;

  private final BasicInfo basicInfo;
  private final TypeElement referencedClass;
  private final ExpectedType<E> expectedType;

  public ReferenceTool(ExpectedType<E> expectedType, BasicInfo basicInfo, TypeElement referencedClass) {
    this.expectedType = expectedType;
    this.basicInfo = basicInfo;
    this.referencedClass = referencedClass;
    this.resolver = new Resolver(expectedType, basicInfo);
  }

  public ReferencedType<E> getReferencedType() {
    return resolver.typecheck(referencedClass, Supplier.class)
        .fold(this::handleNotSupplier, this::handleSupplier);
  }

  private ReferencedType<E> handleNotSupplier(TypecheckFailure failure) {
    if (failure.isFatal()) {
      throw boom(failure.getMessage());
    }
    Declared<E> expected = resolver.typecheck(referencedClass, expectedType.expectedClass())
        .orElseThrow(f -> boom(f.getMessage()));
    return new ReferencedType<>(expected, false);
  }

  private ReferencedType<E> handleSupplier(Declared<Supplier> declaredSupplier) {
    TypeMirror supplied = declaredSupplier.typeArguments().get(0);
    if (supplied.getKind() != TypeKind.DECLARED) {
      throw unexpectedClassException();
    }
    Declared<E> expected = resolver.typecheck(asDeclared(supplied), expectedType.expectedClass())
        .orElseThrow(f -> boom(f.getMessage()));
    return new ReferencedType<>(expected, true);
  }

  private ValidationException unexpectedClassException() {
    return boom("not a " + expectedType.simpleName() +
        " or Supplier<" + expectedType.simpleName() + ">");
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the " +
        expectedType.name().toLowerCase(Locale.US) + " class: %s.", message));
  }
}
