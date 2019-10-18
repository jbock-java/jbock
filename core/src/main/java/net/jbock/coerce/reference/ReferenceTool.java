package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Resolver;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static net.jbock.compiler.TypeTool.asDeclared;

public class ReferenceTool {

  private final Resolver resolver;

  public enum Expectation {

    MAPPER(Function.class),
    COLLECTOR(Collector.class);

    private final Class<?> expectedClass;

    Expectation(Class<?> expectedClass) {
      this.expectedClass = expectedClass;
    }
  }

  private final Expectation expectation;
  private final BasicInfo basicInfo;
  private final TypeElement referencedClass;
  private final Class<?> expectedClass;

  public ReferenceTool(Expectation expectation, BasicInfo basicInfo, TypeElement referencedClass) {
    this.expectation = expectation;
    this.basicInfo = basicInfo;
    this.referencedClass = referencedClass;
    this.expectedClass = expectation.expectedClass;
    this.resolver = new Resolver(basicInfo.tool());
  }

  public AbstractReferencedType getReferencedType() {
    Optional<DeclaredType> supplierType = resolver.typecheck(referencedClass, Supplier.class);
    if (!supplierType.isPresent()) {
      TypeMirror expectedType = resolver.typecheck(referencedClass, expectedClass)
          .orElseThrow(this::unexpectedClassException);
      return new DirectType(checkRawType(asDeclared(expectedType)));
    }
    List<? extends TypeMirror> typeArgs = checkRawType(supplierType.get()).getTypeArguments();
    if (typeArgs.isEmpty()) {
      throw rawTypeException();
    }
    TypeMirror suppliedType = typeArgs.get(0);
    if (tool().isSameErasure(suppliedType, expectedClass)) {
      // "direct supplier"
      return new SupplierType(checkRawType(asDeclared(suppliedType)), Collections.emptyMap());
    }
    if (suppliedType.getKind() != TypeKind.DECLARED) {
      throw inferenceFailedException();
    }
    return getIndirectSupplierType(asDeclared(suppliedType));
  }

  private SupplierType getIndirectSupplierType(DeclaredType suppliedType) {
    TypeElement suppliedElement = tool().asTypeElement(suppliedType);
    if (suppliedType.getTypeArguments().size() != suppliedElement.getTypeParameters().size()) {
      throw inferenceFailedException();
    }
    DeclaredType expectedType = resolver.typecheck(suppliedElement, expectedClass)
        .orElseThrow(this::unexpectedClassException);
    Map<String, TypeMirror> typevarMapping = createTypevarMapping(
        suppliedType.getTypeArguments(),
        suppliedElement.getTypeParameters());
    return new SupplierType(expectedType, typevarMapping);
  }

  private ValidationException inferenceFailedException() {
    return boom("could not infer type parameters");
  }

  private ValidationException unexpectedClassException() {
    return boom("not a " + expectedClass.getSimpleName() +
        " or Supplier<" + expectedClass.getSimpleName() + ">");
  }

  private ValidationException rawTypeException() {
    return boom("the " + expectedClass.getSimpleName().toLowerCase(Locale.US) +
        " type must be parameterized");
  }

  private DeclaredType checkRawType(DeclaredType mapper) {
    if (tool().isRawType(mapper)) {
      throw rawTypeException();
    }
    return mapper;
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the " +
        expectation.name().toLowerCase(Locale.US) + " class: %s.", message));
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
