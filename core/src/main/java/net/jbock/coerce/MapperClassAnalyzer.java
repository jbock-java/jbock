package net.jbock.coerce;

import net.jbock.coerce.mapper.EnhancedMapperType;
import net.jbock.coerce.reference.AbstractReferencedType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.reference.ReferenceTool.Expectation.MAPPER;

// for when there's no collector
final class MapperClassAnalyzer {

  private final BasicInfo basicInfo;
  private final TypeMirror expectedReturnType;
  private final TypeElement mapperClass;

  MapperClassAnalyzer(BasicInfo basicInfo, TypeMirror expectedReturnType, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.expectedReturnType = expectedReturnType;
    this.mapperClass = mapperClass;
  }

  EnhancedMapperType checkReturnType() {
    commonChecks(basicInfo, mapperClass, "mapper");
    AbstractReferencedType functionType = new ReferenceTool(MAPPER, basicInfo, mapperClass)
        .getReferencedType();
    TypeMirror t = functionType.expectedType.getTypeArguments().get(0);
    TypeMirror r = functionType.expectedType.getTypeArguments().get(1);
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

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the mapper class: %s.", message));
  }

  private TypeTool tool() {
    return basicInfo.tool();
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

    EnhancedMapperType solve() {
      List<? extends TypeParameterElement> typeParameters = mapperClass.getTypeParameters();
      List<TypeMirror> solution = typeParameters.stream()
          .map(this::getSolution)
          .collect(Collectors.toList());
      return EnhancedMapperType.create(functionType.isSupplier(), mapperClass, solution);
    }

    TypeMirror getSolution(TypeParameterElement typeParameter) {
      TypeMirror t = t_result.get(typeParameter.toString());
      TypeMirror r = r_result.get(typeParameter.toString());
      List<? extends TypeMirror> bounds = typeParameter.getBounds();
      if (t != null) {
        if (tool().isOutOfBounds(tool().asType(String.class), bounds)) {
          throw boom("invalid bounds");
        }
        if (r != null && !tool().isSameType(t, r)) {
          throw boom("could not infer type parameters");
        }
      }
      if (r != null) {
        if (tool().isOutOfBounds(r, bounds)) {
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
