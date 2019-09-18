package net.jbock.coerce.reference;

import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Resolver;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static net.jbock.compiler.TypeTool.asDeclared;

public class ReferenceTool {

  public enum Expectation {

    MAPPER(Function.class),
    COLLECTOR(Collector.class);

    Class<?> expectedClass;

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
  }

  public AbstractReferencedType getReferencedType() {
    Optional<DeclaredType> supplier = typecheck(Supplier.class, referencedClass);
    if (!supplier.isPresent()) {
      TypeMirror mapper = typecheck(expectedClass, referencedClass)
          .orElseThrow(this::unexpectedClassException);
      checkRawType(mapper);
      return new DirectType(asDeclared(mapper));
    }
    DeclaredType supplierType = supplier.get();
    List<? extends TypeMirror> typeArgs = asDeclared(supplierType).getTypeArguments();
    if (typeArgs.isEmpty()) {
      throw rawTypeException();
    }
    TypeMirror supplied = typeArgs.get(0);
    if (tool().isSameErasure(supplied, expectedClass)) {
      checkRawType(supplied);
      return new SupplierType(asDeclared(supplied), Collections.emptyMap());
    }
    if (supplied.getKind() != TypeKind.DECLARED) {
      throw inferenceFailedException();
    }
    DeclaredType suppliedType = asDeclared(supplied);
    TypeElement suppliedTypeElement = tool().asTypeElement(suppliedType);
    if (suppliedType.getTypeArguments().size() != suppliedTypeElement.getTypeParameters().size()) {
      throw inferenceFailedException();
    }
    Map<String, TypeMirror> typevarMapping = SupplierType.createTypevarMapping(
        suppliedType.getTypeArguments(),
        suppliedTypeElement.getTypeParameters());
    DeclaredType functionType = typecheck(expectedClass, suppliedTypeElement)
        .orElseThrow(this::unexpectedClassException);
    return new SupplierType(functionType, typevarMapping);
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

  private void checkRawType(TypeMirror mapper) {
    if (tool().isRawType(mapper)) {
      throw rawTypeException();
    }
  }

  private Optional<DeclaredType> typecheck(Class<?> goal, TypeElement start) {
    return Resolver.typecheck(start, goal, tool());
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the " +
        expectation.name().toLowerCase(Locale.US) + " class: %s.", message));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
