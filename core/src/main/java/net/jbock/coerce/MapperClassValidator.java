package net.jbock.coerce;

import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.coerce.reference.ExpectedType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.reference.ExpectedType.MAPPER;

final class MapperClassValidator {

  private final BasicInfo basicInfo;
  private final TypeMirror expectedReturnType;
  private final TypeElement mapperClass;

  MapperClassValidator(BasicInfo basicInfo, TypeMirror expectedReturnType, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.expectedReturnType = expectedReturnType;
    this.mapperClass = mapperClass;
  }

  ReferenceMapperType checkReturnType() {
    commonChecks(basicInfo, mapperClass, "mapper");
    ReferencedType<Function> functionType = new ReferenceTool<>(MAPPER, basicInfo, mapperClass)
        .getReferencedType();
    TypeMirror t = functionType.expectedType().typeArguments().get(0);
    TypeMirror r = functionType.expectedType().typeArguments().get(1);
    Optional<Map<String, TypeMirror>> t_result = tool().unify(tool().asType(String.class), t);
    if (!t_result.isPresent()) {
      throw boom(String.format("The supplied function must take a String argument, but takes %s", t));
    }
    Optional<Map<String, TypeMirror>> r_result = tool().unify(expectedReturnType, r);
    if (!r_result.isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", expectedReturnType, r));
    }
    return new Solver(functionType, mergeResult(MAPPER, basicInfo, t_result.get(), r_result.get())).solve();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s.", message));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private class Solver {

    final ReferencedType functionType;
    final Map<String, TypeMirror> result;

    Solver(ReferencedType functionType, Map<String, TypeMirror> result) {
      this.functionType = functionType;
      this.result = result;
    }

    ReferenceMapperType solve() {
      List<? extends TypeParameterElement> typeParameters = mapperClass.getTypeParameters();
      List<TypeMirror> solution = typeParameters.stream()
          .map(p -> MapperClassValidator.getSolution(MAPPER, basicInfo, result, p))
          .collect(Collectors.toList());
      return MapperType.create(tool(), functionType.isSupplier(), mapperClass, solution);
    }
  }

  public static TypeMirror getSolution(
      ExpectedType expectedType,
      BasicInfo basicInfo,
      Map<String, TypeMirror> result,
      TypeParameterElement typeParameter) {
    TypeMirror t = result.get(typeParameter.toString());
    List<? extends TypeMirror> bounds = typeParameter.getBounds();
    if (t != null) {
      if (basicInfo.tool().isOutOfBounds(t, bounds)) {
        throw expectedType.boom(basicInfo, "invalid bounds");
      }
    }
    return t;
  }

  public static Map<String, TypeMirror> mergeResult(
      ExpectedType expectedType,
      BasicInfo basicInfo,
      Map<String, TypeMirror>... results) {
    Map<String, TypeMirror> result = new LinkedHashMap<>();
    for (Map<String, TypeMirror> m : results) {
      for (Map.Entry<String, TypeMirror> entry : m.entrySet()) {
        TypeMirror current = result.get(entry.getKey());
        if (current != null) {
          if (!basicInfo.tool().isSameType(current, entry.getValue())) {
            throw expectedType.boom(basicInfo, "invalid bounds");
          }
        }
        result.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
}
