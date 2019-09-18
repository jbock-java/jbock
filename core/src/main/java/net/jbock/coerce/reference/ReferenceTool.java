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
import java.util.function.Supplier;

import static net.jbock.compiler.TypeTool.asDeclared;

public class ReferenceTool {

  public enum Name {
    MAPPER, COLLECTOR
  }

  private final Name name;
  private final BasicInfo basicInfo;
  private final TypeElement referencedClass;
  private final Class<?> expectedClass;

  public ReferenceTool(Name name, BasicInfo basicInfo, TypeElement referencedClass, Class<?> expectedClass) {
    this.name = name;
    this.basicInfo = basicInfo;
    this.referencedClass = referencedClass;
    this.expectedClass = expectedClass;
  }

  public AbstractReferencedType getReferencedType() {
    Optional<DeclaredType> supplier = typecheck(Supplier.class, referencedClass);
    if (!supplier.isPresent()) {
      TypeMirror mapper = typecheck(expectedClass, referencedClass)
          .orElseThrow(() -> boom("not a " + expectedClass.getSimpleName() +
              " or Supplier<" + expectedClass.getSimpleName() + ">"));
      if (tool().isRawType(mapper)) {
        throw boom("the " +
            expectedClass.getSimpleName().toLowerCase(Locale.US) +
            " type must be parameterized");
      }
      return new DirectType(asDeclared(mapper));
    }
    DeclaredType supplierType = supplier.get();
    List<? extends TypeMirror> typeArgs = asDeclared(supplierType).getTypeArguments();
    if (typeArgs.isEmpty()) {
      throw boom("raw Supplier type");
    }
    if (tool().isSameErasure(typeArgs.get(0), expectedClass)) {
      if (tool().isRawType(typeArgs.get(0))) {
        throw boom("the " +
            expectedClass.getSimpleName().toLowerCase(Locale.US) +
            " type must be parameterized");
      }
      return new SupplierType(asDeclared(typeArgs.get(0)), Collections.emptyMap());
    }
    if (typeArgs.get(0).getKind() != TypeKind.DECLARED) {
      throw boom("could not infer type parameters");
    }
    DeclaredType suppliedType = asDeclared(typeArgs.get(0));
    TypeElement suppliedTypeElement = tool().asTypeElement(suppliedType);
    if (suppliedType.getTypeArguments().size() != suppliedTypeElement.getTypeParameters().size()) {
      throw boom("could not infer type parameters");
    }
    Map<String, TypeMirror> typevarMapping = SupplierType.createTypevarMapping(
        suppliedType.getTypeArguments(),
        suppliedTypeElement.getTypeParameters());
    DeclaredType functionType = typecheck(expectedClass, suppliedTypeElement).orElseThrow(() ->
        boom("not a " + expectedClass.getSimpleName() +
            " or Supplier<" + expectedClass.getSimpleName() + ">"));
    return new SupplierType(functionType, typevarMapping);
  }

  private Optional<DeclaredType> typecheck(Class<?> goal, TypeElement start) {
    return Resolver.typecheck(start, goal, tool());
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the " +
        name.name().toLowerCase(Locale.US) + " class: %s.", message));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
