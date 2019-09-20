package net.jbock.coerce;

import net.jbock.coerce.mapper.EnhancedMapperType;
import net.jbock.coerce.reference.AbstractReferencedType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
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
  private final TypeMirror originalReturnType;
  private final TypeElement mapperClass;

  MapperClassAnalyzer(BasicInfo basicInfo, TypeMirror originalReturnType, TypeElement mapperClass) {
    this.basicInfo = basicInfo;
    this.originalReturnType = originalReturnType;
    this.mapperClass = mapperClass;
  }

  EnhancedMapperType checkReturnType() {
    commonChecks(basicInfo, mapperClass, "mapper");
    AbstractReferencedType functionType = new ReferenceTool(MAPPER, basicInfo, mapperClass)
        .getReferencedType();
    TypeMirror t = functionType.expectedType.getTypeArguments().get(0);
    TypeMirror r = functionType.expectedType.getTypeArguments().get(1);
    Map<String, TypeMirror> t_result = tool().unify(tool().asType(String.class), t)
        .map(functionType::mapTypevars)
        .orElseThrow(() -> boom(String.format("The supplied function must take a String argument, but takes %s", t)));
    Optional<Map<String, TypeMirror>> r_result = tool().unify(originalReturnType, r);
    boolean optional = false;
    if (r_result.isPresent()) {
      Compatibility compatibility = checkCompat(t_result, r_result.get());
      if (compatibility instanceof CompatibleViaOptional) {
        Map<String, TypeMirror> m = new HashMap<>(r_result.get());
        m.put(((CompatibleViaOptional) compatibility).key, ((CompatibleViaOptional) compatibility).solution);
        r_result = Optional.of(m);
        optional = true;
      }
    }
    if (!r_result.isPresent()) {
      r_result = tool().unify(originalReturnType, tool().optionalOf(r));
      optional = true;
    }
    if (!r_result.isPresent()) {
      throw boom(String.format("The mapper should return %s but returns %s", originalReturnType, r));
    }
    return new Solver(functionType, t_result, functionType.mapTypevars(r_result.get()), optional).solve();
  }

  class Compatibility {
  }

  class Compatible extends Compatibility {
  }

  class CompatibleViaOptional extends Compatibility {
    final String key;
    final TypeMirror solution;

    CompatibleViaOptional(String key, TypeMirror solution) {
      this.key = key;
      this.solution = solution;
    }
  }


  private Compatibility checkCompat(Map<String, TypeMirror> t_result, Map<String, TypeMirror> r_result) {
    if (t_result.isEmpty()) {
      return new Compatible();
    }
    Map.Entry<String, TypeMirror> tEntry = t_result.entrySet().iterator().next();
    String key = tEntry.getKey();
    TypeMirror tSolution = tEntry.getValue();
    TypeMirror rSolution = r_result.get(key);
    if (rSolution == null) {
      return new Compatible();
    }
    if (tool().isSameType(tSolution, rSolution)) {
      return new Compatible();
    }
    if (tool().unify(rSolution, tool().optionalOf(tSolution)).isPresent()) {
      return new CompatibleViaOptional(key, tSolution);
    }
    throw boom("could not infer type parameters");
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
    final boolean optional;

    Solver(
        AbstractReferencedType functionType,
        Map<String, TypeMirror> t_result,
        Map<String, TypeMirror> r_result,
        boolean optional) {
      this.functionType = functionType;
      this.t_result = t_result;
      this.r_result = r_result;
      this.optional = optional;
    }

    EnhancedMapperType solve() {
      List<? extends TypeParameterElement> typeParameters = mapperClass.getTypeParameters();
      List<TypeMirror> solution = typeParameters.stream()
          .map(this::getSolution)
          .collect(Collectors.toList());
      return EnhancedMapperType.create(functionType.isSupplier(), optional, mapperClass, solution);
    }

    TypeMirror getSolution(TypeParameterElement typeParameter) {
      TypeMirror t = t_result.get(typeParameter.toString());
      TypeMirror r = r_result.get(typeParameter.toString());
      List<? extends TypeMirror> bounds = typeParameter.getBounds();
      if (t != null) {
        if (tool().isOutOfBounds(tool().asType(String.class), bounds)) {
          throw boom("invalid bounds");
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
