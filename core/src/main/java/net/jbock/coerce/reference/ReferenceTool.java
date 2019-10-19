package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.compiler.TypeTool.asDeclared;

public class ReferenceTool<E> {

  private final Resolver resolver;

  private final BasicInfo basicInfo;
  private final TypeElement referencedClass;
  private final ExpectedType<E> expectedClass;

  public ReferenceTool(ExpectedType<E> expectedClass, BasicInfo basicInfo, TypeElement referencedClass) {
    this.expectedClass = expectedClass;
    this.basicInfo = basicInfo;
    this.referencedClass = referencedClass;
    this.resolver = new Resolver(basicInfo);
  }

  public ReferencedType<E> getReferencedType() {
    Optional<Declared<Supplier>> supplierType = resolver.typecheck(referencedClass, Supplier.class);
    if (!supplierType.isPresent()) {
      Declared<E> expectedType = resolver.typecheck(referencedClass, expectedClass.expectedClass())
          .orElseThrow(this::unexpectedClassException);
      return new DirectType<>(checkRawType(expectedType));
    }
    List<? extends TypeMirror> typeArgs = checkRawType(supplierType.get()).typeArguments();
    TypeMirror supplied = typeArgs.get(0);
    if (supplied.getKind() != TypeKind.DECLARED) {
      throw unexpectedClassException();
    }
    DeclaredType suppliedType = asDeclared(supplied);
    Optional<Declared<E>> directExpectation = resolver.typecheck(suppliedType, expectedClass.expectedClass());
    if (directExpectation.isPresent() && directExpectation.get().isDirect()) {
      // "direct supplier"
      return new SupplierType<>(checkRawType(directExpectation.get()), Collections.emptyMap());
    }
    if (supplied.getKind() != TypeKind.DECLARED) {
      throw inferenceFailedException();
    }
    return getIndirectExpectation(suppliedType);
  }

  private SupplierType<E> getIndirectExpectation(DeclaredType suppliedType) {
    TypeElement suppliedElement = tool().asTypeElement(suppliedType);
    if (suppliedType.getTypeArguments().size() != suppliedElement.getTypeParameters().size()) {
      throw inferenceFailedException();
    }
    Declared<E> expectedType = resolver.typecheck(suppliedElement, expectedClass.expectedClass())
        .orElseThrow(this::unexpectedClassException);
    Map<String, TypeMirror> typevarMapping = createTypevarMapping(
        suppliedType.getTypeArguments(),
        suppliedElement.getTypeParameters());
    return new SupplierType<>(expectedType, typevarMapping);
  }

  private ValidationException inferenceFailedException() {
    return boom("could not infer type parameters");
  }

  private ValidationException unexpectedClassException() {
    return boom("not a " + expectedClass.simpleName() +
        " or Supplier<" + expectedClass.simpleName() + ">");
  }

  private ValidationException rawTypeException() {
    return boom("the " + expectedClass.simpleName().toLowerCase(Locale.US) +
        " type must be parameterized");
  }

  private <P> Declared<P> checkRawType(Declared<P> mapper) {
    if (tool().isRawType(mapper.asType(tool()))) {
      throw rawTypeException();
    }
    return mapper;
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the " +
        expectedClass.name().toLowerCase(Locale.US) + " class: %s.", message));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private static Map<String, TypeMirror> createTypevarMapping(
      List<? extends TypeMirror> typeArguments,
      List<? extends TypeParameterElement> typeParameters) {
    Map<String, TypeMirror> mapping = new LinkedHashMap<>();
    for (int i = 0; i < typeParameters.size(); i++) {
      TypeParameterElement p = typeParameters.get(i);
      TypeMirror typeArgument = typeArguments.get(i);
      if (typeArgument.getKind() == TypeKind.TYPEVAR) {
        mapping.put(p.toString(), typeArgument);
      }
    }
    return mapping;
  }
}
