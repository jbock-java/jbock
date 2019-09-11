package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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

  net.jbock.coerce.MapperType checkReturnType() {
    commonChecks(basicInfo, mapperClass, "mapper");
    MapperType mapperType = getMapperType();
    TypeMirror string = tool().asType(String.class);
    TypeMirror t = asDeclared(mapperType.functionType).getTypeArguments().get(0);
    TypeMirror r = asDeclared(mapperType.functionType).getTypeArguments().get(1);
    Optional<Map<String, TypeMirror>> t_result = tool().unify(string, t);
    if (!t_result.isPresent()) {
      throw boom(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    Optional<Map<String, TypeMirror>> r_result = tool().unify(expectedReturnType, r);
    if (!r_result.isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
    return solve(mapperType, t_result.get(), r_result.get());
  }

  private MapperType getMapperType() {
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

  private static class MapperType {

    final TypeMirror functionType; // subtype of Function
    final boolean supplier; // wrapped in Supplier?

    MapperType(TypeMirror functionType, boolean supplier) {
      this.functionType = functionType;
      this.supplier = supplier;
    }
  }

  private MapperType mapperType(
      TypeMirror type,
      boolean supplier) {
    if (!tool().isSameErasure(type, Function.class)) {
      throw boom("must either implement Function or Supplier<Function>");
    }
    if (tool().isRawType(type)) {
      throw boom("the function type must be parameterized");
    }
    return new MapperType(type, supplier);
  }

  private net.jbock.coerce.MapperType solve(
      MapperType mapperType,
      Map<String, TypeMirror> t_result,
      Map<String, TypeMirror> r_result) {
    List<? extends TypeParameterElement> typeParameters = mapperClass.getTypeParameters();
    List<TypeMirror> solution = new ArrayList<>(typeParameters.size());
    for (TypeParameterElement typeParameter : typeParameters) {
      String param = typeParameter.toString();
      TypeMirror tMirror = t_result.get(param);
      TypeMirror rMirror = r_result.get(param);
      TypeMirror s = null;
      if (tMirror != null) {
        if (isInvalidT(typeParameter)) {
          throw boom("invalid bounds");
        }
        s = tMirror;
      }
      if (rMirror != null) {
        if (isInvalidR(typeParameter)) {
          throw boom("invalid bounds");
        }
        s = rMirror;
      }
      if (s == null) {
        throw boom("could not resolve all type parameters");
      }
      solution.add(s);
    }
    return net.jbock.coerce.MapperType.create(mapperType.supplier, mapperClass, solution);
  }
}
