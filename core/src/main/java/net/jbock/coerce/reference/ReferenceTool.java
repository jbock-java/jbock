package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.TypeTool;
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
    Either<String, Declared<Supplier>> supplierType = resolver.typecheck(referencedClass, Supplier.class);
    if (supplierType instanceof Left) {
      Declared<E> expectedType = resolver.typecheck(referencedClass, this.expectedType.expectedClass())
          .orElseThrow(this::boom);
      return new ReferencedType<>(checkRawType(expectedType), false);
    }
    List<? extends TypeMirror> typeArgs = checkRawType(((Right<String, Declared<Supplier>>) supplierType).value()).typeArguments();
    TypeMirror supplied = typeArgs.get(0);
    if (supplied.getKind() != TypeKind.DECLARED) {
      throw unexpectedClassException();
    }
    DeclaredType suppliedType = asDeclared(supplied);
    Declared<E> expected = resolver.typecheck(suppliedType, expectedType.expectedClass())
        .map(this::checkRawType)
        .orElseThrow(this::boom);
    if (!expected.isDirect()) {
      throw unexpectedClassException();
    }
    return new ReferencedType<>(checkRawType(expected), true);
  }

  private ValidationException unexpectedClassException() {
    return boom("not a " + expectedType.simpleName() +
        " or Supplier<" + expectedType.simpleName() + ">");
  }

  private <P> Declared<P> checkRawType(Declared<P> mapper) {
    if (tool().isRawType(mapper.asType(tool()))) {
      throw boom("the " + expectedType.simpleName().toLowerCase(Locale.US) +
          " type must be parameterized");
    }
    return mapper;
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the " +
        expectedType.name().toLowerCase(Locale.US) + " class: %s.", message));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
