package net.jbock.coerce.collectorpresent;

import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Flattener;
import net.jbock.coerce.FlattenerResult;
import net.jbock.coerce.collectors.CustomCollector;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.coerce.reference.TypecheckFailure;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.TypevarMapping;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.Util.checkNotAbstract;
import static net.jbock.coerce.reference.ExpectedType.COLLECTOR;

public class CollectorClassValidator {

  private final BasicInfo basicInfo;
  private final TypeElement collectorClass;
  private final Optional<TypeMirror> mapperPreference;

  public CollectorClassValidator(BasicInfo basicInfo, TypeElement collectorClass, Optional<TypeMirror> mapperPreference) {
    this.basicInfo = basicInfo;
    this.collectorClass = collectorClass;
    this.mapperPreference = mapperPreference;
  }

  // visible for testing
  public CustomCollector getCollectorInfo() {
    commonChecks(collectorClass);
    checkNotAbstract(collectorClass);
    ReferencedType<Collector> collectorType = new ReferenceTool<>(COLLECTOR, basicInfo, collectorClass)
        .getReferencedType();
    TypeMirror t = collectorType.typeArguments().get(0);
    TypeMirror r = collectorType.typeArguments().get(2);
    TypevarMapping r_result = tool().unify(basicInfo.originalReturnType(), r)
        .orElseThrow(this::boom);
    TypevarMapping t_result = getTResult(t).map(m -> new TypevarMapping(Collections.singletonMap(t.toString(), m), tool()))
        .orElse(TypevarMapping.empty(tool()));
    FlattenerResult typeParameters = new Flattener(basicInfo, collectorClass, mapperPreference.map(preference -> new Flattener.Preference(t.toString(), preference)))
        .getTypeParameters(t_result, r_result)
        .orElseThrow(this::boom);
    TypeMirror inputType = typeParameters.substitute(t).orElseThrow(f -> boom(f.getMessage()));
    if (mapperPreference.isPresent()) {
      TypeMirror preference = mapperPreference.get();
      TypeMirror inferred = typeParameters.substitute(preference).orElseThrow(f -> boom(f.getMessage()));
      Either<Function<String, String>, TypeMirror> specialization = tool().getSpecialization(inferred, inputType);
      if (specialization instanceof Left) {
        Either<String, TypevarMapping> unify = tool().unify(preference, inputType);
        Either<TypecheckFailure, TypeMirror> subsitute = unify
            .orElseThrow(f -> boom(((Left<Function<String, String>, TypeMirror>) specialization).value().apply("collector input")))
            .substitute(inputType);
        inputType = subsitute.orElseThrow(f -> boom(f.getMessage()));
      } else {
        inputType = ((Right<Function<String, String>, TypeMirror>) specialization).value();
      }
    }
    if (inputType.getKind() == TypeKind.TYPEVAR) {
      inputType = tool().getDeclaredType(String.class, Collections.emptyList());
    }
    return new CustomCollector(tool(), inputType, collectorClass, collectorType.isSupplier(), typeParameters.getTypeParameters());
  }

  private Optional<TypeMirror> getTResult(TypeMirror t) {
    if (!mapperPreference.isPresent()) {
      return Optional.empty();
    }
    if (t.getKind() == TypeKind.TYPEVAR) {
      TypeParameterElement p = findByName(t.toString()).getValue();
      return basicInfo.tool().getBound(p)
          .map(Function.identity(), Optional::of)
          .orElseThrow(this::boom);
    }
    return Optional.empty();
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(COLLECTOR.boom(message));
  }

  private Map.Entry<Integer, TypeParameterElement> findByName(String t) {
    List<? extends TypeParameterElement> typeParameters = collectorClass.getTypeParameters();
    for (int i = 0; i < typeParameters.size(); i++) {
      TypeParameterElement p = typeParameters.get(i);
      if (p.toString().equals(t)) {
        return new AbstractMap.SimpleImmutableEntry<>(i, p);
      }
    }
    throw new AssertionError("expecting a type parameter named " + t.toString());
  }
}
