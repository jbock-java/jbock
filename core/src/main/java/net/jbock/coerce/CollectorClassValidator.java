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
import java.util.function.Supplier;
import java.util.stream.Collector;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.asDeclared;

class CollectorClassValidator {

  private final BasicInfo basicInfo;

  CollectorClassValidator(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  private boolean isInvalidR(TypeParameterElement typeParameter, TypeMirror value) {
    for (TypeMirror bound : typeParameter.getBounds()) {
      if (!tool().isAssignable(value, bound)) {
        return true;
      }
    }
    return false;
  }

  // visible for testing
  CollectorInfo getCollectorInfo(TypeElement collectorClass) {
    commonChecks(basicInfo, collectorClass, "collector");
    TmpCollectorType collectorType = getCollectorType(collectorClass);
    TypeMirror t = asDeclared(collectorType.type).getTypeArguments().get(0);
    TypeMirror r = asDeclared(collectorType.type).getTypeArguments().get(2);
    Map<String, TypeMirror> r_result = tool().unify(basicInfo.returnType(), r)
        .orElseThrow(() -> boom(String.format("The collector should return %s but returns %s", basicInfo.returnType(), r)));
    if (!tool().isAssignableToTypeElement(collectorClass.asType())) {
      throw boom("invalid bounds");
    }
    TypeMirror collectorInput = tool().substitute(t, r_result);
    if (collectorInput == null) {
      throw boom("could not resolve all type parameters");
    }
    return CollectorInfo.create(collectorInput, solve(collectorType, r_result));
  }

  private TmpCollectorType getCollectorType(TypeElement collectorClass) {
    Optional<TypeMirror> supplier = Resolver.typecheck(
        Supplier.class,
        collectorClass.asType(),
        basicInfo.tool());
    if (supplier.isPresent()) {
      List<? extends TypeMirror> typeArgs = asDeclared(supplier.get()).getTypeArguments();
      if (typeArgs.isEmpty()) {
        throw boom("raw Supplier type");
      }
      return TmpCollectorType.create(basicInfo, typeArgs.get(0), true, collectorClass);
    }
    TypeMirror collector = Resolver.typecheck(
        Collector.class,
        collectorClass.asType(),
        basicInfo.tool()).orElseThrow(() ->
        boom("not a Collector or Supplier<Collector>"));
    return TmpCollectorType.create(basicInfo, collector, false, collectorClass);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the collector class: %s.", message));
  }

  private static final class TmpCollectorType {

    final TypeElement collectorClass; // implements Collector or Supplier<Collector>
    final TypeMirror type; // subtype of Collector
    final boolean supplier; // wrapped in Supplier?

    TmpCollectorType(TypeElement collectorClass, TypeMirror type, boolean supplier) {
      this.collectorClass = collectorClass;
      this.type = type;
      this.supplier = supplier;
    }

    static TmpCollectorType create(BasicInfo basicInfo, TypeMirror type, boolean supplier, TypeElement collectorClass) {
      if (!basicInfo.tool().isSameErasure(type, Collector.class)) {
        throw boom(basicInfo, "must either implement Collector or Supplier<Collector>");
      }
      if (basicInfo.tool().isRawType(type)) {
        throw boom(basicInfo, "the collector type must be parameterized");
      }
      return new TmpCollectorType(collectorClass, type, supplier);
    }

    static ValidationException boom(BasicInfo basicInfo, String message) {
      return basicInfo.asValidationException(String.format("There is a problem with the collector class: %s", message));
    }
  }

  private CollectorType solve(
      TmpCollectorType collectorType,
      Map<String, TypeMirror> r_result) {
    List<? extends TypeParameterElement> typeParameters = collectorType.collectorClass.getTypeParameters();
    List<TypeMirror> solution = new ArrayList<>(typeParameters.size());
    for (TypeParameterElement typeParameter : typeParameters) {
      String param = typeParameter.toString();
      TypeMirror rMirror = r_result.get(param);
      TypeMirror s = null;
      if (rMirror != null) {
        if (isInvalidR(typeParameter, rMirror)) {
          throw boom("invalid bounds");
        }
        s = rMirror;
      }
      solution.add(s);
    }
    return CollectorType.create(collectorType.supplier, collectorType.collectorClass, solution);
  }
}
