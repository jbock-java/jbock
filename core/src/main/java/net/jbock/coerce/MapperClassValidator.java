package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
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

  private boolean isInvalidT(TypeParameterElement typeParameter) {
    TypeMirror stringType = tool().asType(String.class);
    for (TypeMirror bound : typeParameter.getBounds()) {
      if (!tool().isAssignable(stringType, bound)) {
        return true;
      }
    }
    return false;
  }

  private boolean isInvalidR(TypeParameterElement typeParameter) {
    for (TypeMirror bound : typeParameter.getBounds()) {
      if (!tool().isAssignable(expectedReturnType, bound)) {
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
    FunctionType functionType = getMapperType();
    TypeMirror string = tool().asType(String.class);
    TypeMirror t = asDeclared(functionType.functionType).getTypeArguments().get(0);
    TypeMirror r = asDeclared(functionType.functionType).getTypeArguments().get(1);
    Optional<Map<String, TypeMirror>> t_result = tool().unify(string, t);
    if (!t_result.isPresent()) {
      throw boom(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    Optional<Map<String, TypeMirror>> r_result = tool().unify(expectedReturnType, r);
    if (!r_result.isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
    return new Solver(functionType.supplier, t_result.get(), r_result.get()).solve();
  }

  private FunctionType getMapperType() {
    Optional<TypeMirror> supplier = typecheck(Supplier.class, mapperClass);
    if (supplier.isPresent()) {
      List<? extends TypeMirror> typeArgs = asDeclared(supplier.get()).getTypeArguments();
      if (typeArgs.isEmpty()) {
        throw boom("raw Supplier type");
      }
      return mapperType(typeArgs.get(0), true);
    }
    TypeMirror mapper = typecheck(Function.class, mapperClass)
        .orElseThrow(() ->
            boom("not a Function or Supplier<Function>"));
    return mapperType(mapper, false);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private Optional<TypeMirror> typecheck(Class<?> goal, TypeElement start) {
    return Resolver.typecheck(goal, start, tool());
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s.", message));
  }

  private static class FunctionType {

    final TypeMirror functionType; // subtype of Function
    final boolean supplier; // wrapped in Supplier?

    FunctionType(TypeMirror functionType, boolean supplier) {
      this.functionType = functionType;
      this.supplier = supplier;
    }
  }

  private FunctionType mapperType(
      TypeMirror type,
      boolean supplier) {
    if (!tool().isSameErasure(type, Function.class)) {
      throw boom("must either implement Function or Supplier<Function>");
    }
    if (tool().isRawType(type)) {
      throw boom("the function type must be parameterized");
    }
    return new FunctionType(type, supplier);
  }

  private class Solver {

    final boolean isSupplier;
    final Map<String, TypeMirror> t_result;
    final Map<String, TypeMirror> r_result;

    Solver(boolean isSupplier, Map<String, TypeMirror> t_result, Map<String, TypeMirror> r_result) {
      this.isSupplier = isSupplier;
      this.t_result = t_result;
      this.r_result = r_result;
    }

    MapperType solve() {
      List<? extends TypeParameterElement> typeParameters = mapperClass.getTypeParameters();
      List<TypeMirror> solution = typeParameters.stream()
          .map(this::getSolution)
          .collect(Collectors.toList());
      return MapperType.create(isSupplier, mapperClass, solution);
    }

    TypeMirror getSolution(TypeParameterElement typeParameter) {
      TypeMirror t = t_result.get(typeParameter.toString());
      TypeMirror r = r_result.get(typeParameter.toString());
      if (t != null) {
        if (isInvalidT(typeParameter)) {
          throw boom("invalid bounds");
        }
        if (r != null && !tool().isSameType(t, r)) {
          throw boom("could not infer type parameters");
        }
      }
      if (r != null) {
        if (isInvalidR(typeParameter)) {
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
