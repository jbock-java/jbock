package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
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
    Either<TypecheckFailure, Declared<Supplier>> supplierType = resolver.typecheck(referencedClass, Supplier.class);
    if (supplierType.failureMatches(TypecheckFailure::isFatal)) {
      throw boom(((Left<TypecheckFailure, Declared<Supplier>>) supplierType).value().getMessage());
    }
    if (supplierType instanceof Left) {
      Declared<E> expectedType = resolver.typecheck(referencedClass, this.expectedType.expectedClass())
          .orElseThrow(f -> boom(f.getMessage()));
      return new ReferencedType<>(expectedType, false);
    }
    List<? extends TypeMirror> typeArgs = ((Right<TypecheckFailure, Declared<Supplier>>) supplierType).value().typeArguments();
    TypeMirror supplied = typeArgs.get(0);
    if (supplied.getKind() != TypeKind.DECLARED) {
      throw unexpectedClassException();
    }
    DeclaredType suppliedType = asDeclared(supplied);
    Declared<E> expected = resolver.typecheck(suppliedType, expectedType.expectedClass())
        .orElseThrow(f -> boom(f.getMessage()));
    if (!expected.isDirect()) {
      throw unexpectedClassException();
    }
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
