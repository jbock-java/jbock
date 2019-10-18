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
import java.util.HashMap;
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
    this.resolver = new Resolver(basicInfo.tool());
  }

  public AbstractReferencedType<E> getReferencedType() {
    Optional<Declared<Supplier>> supplierType = resolver.typecheck(referencedClass, Supplier.class);
    if (!supplierType.isPresent()) {
      Declared<E> expectedType = resolver.typecheck(referencedClass, expectedClass.expectedClass())
          .orElseThrow(this::unexpectedClassException);
      return new DirectType<>(checkRawType(expectedType));
    }
    List<? extends TypeMirror> typeArgs = checkRawType(supplierType.get()).typeArguments();
    TypeMirror suppliedType = typeArgs.get(0);
    if (tool().isSameErasure(suppliedType, expectedClass.expectedClass())) {
      // "direct supplier"
      return new SupplierType<>(checkRawType(new Declared<>(expectedClass.expectedClass(),
          asDeclared(suppliedType).getTypeArguments())), Collections.emptyMap());
    }
    if (suppliedType.getKind() != TypeKind.DECLARED) {
      throw inferenceFailedException();
    }
    return getIndirectSupplierType(asDeclared(suppliedType));
  }

  private SupplierType<E> getIndirectSupplierType(DeclaredType suppliedType) {
    TypeElement suppliedElement = tool().asTypeElement(suppliedType);
    if (suppliedType.getTypeArguments().size() != suppliedElement.getTypeParameters().size()) {
      throw inferenceFailedException();
    }
    Optional<Declared<E>> tmp = resolver.typecheck(suppliedElement, expectedClass.expectedClass());
    Declared<E> expectedType = tmp.orElseThrow(this::unexpectedClassException);
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

  private <E> Declared<E> checkRawType(Declared<E> mapper) {
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
    Map<String, TypeMirror> mapping = new HashMap<>();
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
