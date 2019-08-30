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

  MapperType checkReturnType() {
    commonChecks(basicInfo, mapperClass, "mapper");
    TmpMapperType mapperType = getMapperType(mapperClass);
    TypeMirror string = tool().asType(String.class);
    TypeMirror t = asDeclared(mapperType.type).getTypeArguments().get(0);
    TypeMirror r = asDeclared(mapperType.type).getTypeArguments().get(1);
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

  private TmpMapperType getMapperType(TypeElement mapperClass) {
    Optional<TypeMirror> supplier = Resolver.typecheck(
        Supplier.class,
        mapperClass.asType(),
        basicInfo.tool());
    if (supplier.isPresent()) {
      List<? extends TypeMirror> typeArgs = asDeclared(supplier.get()).getTypeArguments();
      if (typeArgs.isEmpty()) {
        throw boom("raw Supplier type");
      }
      return TmpMapperType.create(basicInfo, typeArgs.get(0), true, mapperClass);
    }
    TypeMirror mapper = Resolver.typecheck(
        Function.class,
        mapperClass.asType(),
        basicInfo.tool()).orElseThrow(() ->
        boom("not a Function or Supplier<Function>"));
    return TmpMapperType.create(basicInfo, mapper, false, mapperClass);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s.", message));
  }

  static final class TmpMapperType {

    private final TypeElement mapperClass; // implements Function or Supplier<Function>
    private final TypeMirror type; // subtype of Function
    private final boolean supplier; // wrapped in Supplier?

    private TmpMapperType(TypeElement mapperClass, TypeMirror type, boolean supplier) {
      this.mapperClass = mapperClass;
      this.type = type;
      this.supplier = supplier;
    }

    static TmpMapperType create(
        BasicInfo basicInfo,
        TypeMirror type,
        boolean supplier,
        TypeElement mapperClass) {
      if (!basicInfo.tool().isSameErasure(type, Function.class)) {
        throw boom(basicInfo, "must either implement Function or Supplier<Function>");
      }
      if (basicInfo.tool().isRawType(type)) {
        throw boom(basicInfo, "the function type must be parameterized");
      }
      return new TmpMapperType(mapperClass, type, supplier);
    }

    private static ValidationException boom(BasicInfo basicInfo, String message) {
      return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s", message));
    }
  }

  private MapperType solve(
      TmpMapperType mapperType,
      Map<String, TypeMirror> t_result,
      Map<String, TypeMirror> r_result) {
    List<? extends TypeParameterElement> typeParameters = mapperType.mapperClass.getTypeParameters();
    List<TypeMirror> solution = new ArrayList<>(typeParameters.size());
    for (TypeParameterElement typeParameter : typeParameters) {
      String param = typeParameter.toString();
      TypeMirror tMirror = t_result.get(param);
      TypeMirror rMirror = r_result.get(param);
      TypeMirror s = null;
      if (tMirror != null) {
        if (isInvalidT(typeParameter)) {
          throw boom("Invalid bounds on the type parameters of the mapper class");
        }
        s = tMirror;
      }
      if (rMirror != null) {
        if (isInvalidR(typeParameter)) {
          throw boom("Invalid bounds on the type parameters of the mapper class");
        }
        s = rMirror;
      }
      if (s == null) {
        throw boom("could not resolve all type parameters");
      }
      solution.add(s);
    }
    return MapperType.create(basicInfo, mapperType.supplier, mapperClass, solution);
  }
}
