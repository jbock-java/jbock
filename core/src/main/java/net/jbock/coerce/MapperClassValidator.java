package net.jbock.coerce;

import net.jbock.coerce.reference.AbstractReferencedType;
import net.jbock.coerce.reference.DirectType;
import net.jbock.coerce.reference.SupplierType;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.asDeclared;

final class MapperClassValidator {

  private final BasicInfo basicInfo;
  private final TypeMirror expectedReturnType;
  private final TypeElement mapperClass;

  private boolean isOutOfBounds(TypeMirror mirror, List<? extends TypeMirror> bounds) {
    for (TypeMirror bound : bounds) {
      if (!tool().isAssignable(mirror, bound)) {
        return true;
      }
    }
    return false;
  }

  MapperClassValidator(BasicInfo basicInfo, TypeMirror expectedReturnType, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.expectedReturnType = expectedReturnType;
    this.mapperClass = mapperClass;
  }

  MapperType checkReturnType() {
    commonChecks(basicInfo, mapperClass, "mapper");
    AbstractReferencedType functionType = getMapperType();
    TypeMirror t = functionType.referencedType.getTypeArguments().get(0);
    TypeMirror r = functionType.referencedType.getTypeArguments().get(1);
    Optional<Map<String, TypeMirror>> t_result = tool().unify(tool().asType(String.class), t)
        .map(functionType::mapTypevars);
    if (!t_result.isPresent()) {
      throw boom(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    Optional<Map<String, TypeMirror>> r_result = tool().unify(expectedReturnType, r)
        .map(functionType::mapTypevars);
    if (!r_result.isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
    return new Solver(functionType, t_result.get(), r_result.get()).solve();
  }

  private AbstractReferencedType getMapperType() {
    Optional<DeclaredType> supplier = typecheck(Supplier.class, mapperClass);
    if (supplier.isPresent()) {
      DeclaredType supplierType = supplier.get();
      List<? extends TypeMirror> typeArgs = asDeclared(supplierType).getTypeArguments();
      if (typeArgs.isEmpty()) {
        throw boom("raw Supplier type");
      }
      if (tool().isSameErasure(typeArgs.get(0), Function.class)) {
        if (tool().isRawType(typeArgs.get(0))) {
          throw boom("the function type must be parameterized");
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
      DeclaredType functionType = typecheck(Function.class, suppliedTypeElement).orElseThrow(() ->
          boom("not a Function or Supplier<Function>"));
      return new SupplierType(functionType, typevarMapping);
    }
    TypeMirror mapper = typecheck(Function.class, mapperClass)
        .orElseThrow(() -> boom("not a Function or Supplier<Function>"));
    if (tool().isRawType(mapper)) {
      throw boom("the function type must be parameterized");
    }
    return new DirectType(asDeclared(mapper));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private Optional<DeclaredType> typecheck(Class<?> goal, TypeElement start) {
    return Resolver.typecheck(start, goal, tool());
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s.", message));
  }

  private class Solver {

    final AbstractReferencedType functionType;
    final Map<String, TypeMirror> t_result;
    final Map<String, TypeMirror> r_result;

    Solver(AbstractReferencedType functionType, Map<String, TypeMirror> t_result, Map<String, TypeMirror> r_result) {
      this.functionType = functionType;
      this.t_result = t_result;
      this.r_result = r_result;
    }

    MapperType solve() {
      List<? extends TypeParameterElement> typeParameters = mapperClass.getTypeParameters();
      List<TypeMirror> solution = typeParameters.stream()
          .map(this::getSolution)
          .collect(Collectors.toList());
      return MapperType.create(functionType instanceof SupplierType, mapperClass, solution);
    }

    TypeMirror getSolution(TypeParameterElement typeParameter) {
      TypeMirror t = t_result.get(typeParameter.toString());
      TypeMirror r = r_result.get(typeParameter.toString());
      List<? extends TypeMirror> bounds = typeParameter.getBounds();
      if (t != null) {
        if (isOutOfBounds(tool().asType(String.class), bounds)) {
          throw boom("invalid bounds");
        }
        if (r != null && !tool().isSameType(t, r)) {
          throw boom("could not infer type parameters");
        }
      }
      if (r != null) {
        if (isOutOfBounds(r, bounds)) {
          throw boom("invalid bounds");
        }
      }
      return Stream.of(t, r)
          .filter(Objects::nonNull)
          .findFirst()
          .orElseThrow(() -> boom("could not infer all type parameters"));
    }
  }
}
