package net.jbock.coerce;

import net.jbock.coerce.collector.CustomCollector;
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
  private final TypeElement collectorClass;

  CollectorClassValidator(BasicInfo basicInfo, TypeElement collectorClass) {
    this.basicInfo = basicInfo;
    this.collectorClass = collectorClass;
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
  CustomCollector getCollectorInfo() {
    commonChecks(basicInfo, collectorClass, "collector");
    CollectorType collectorType = getCollectorType();
    TypeMirror t = asDeclared(collectorType.collectorType).getTypeArguments().get(0);
    TypeMirror r = asDeclared(collectorType.collectorType).getTypeArguments().get(2);
    Map<String, TypeMirror> r_result = tool().unify(basicInfo.returnType(), r)
        .orElseThrow(() -> boom(String.format("The collector should return %s but returns %s", basicInfo.returnType(), r)));
    if (!tool().isAssignableToTypeElement(collectorClass.asType())) {
      throw boom("invalid bounds");
    }
    TypeMirror inputType = tool().substitute(t, r_result);
    if (inputType == null) {
      throw boom("could not resolve all type parameters");
    }
    return solve(inputType, collectorType, r_result);
  }

  private CollectorType getCollectorType() {
    Optional<TypeMirror> supplier = typecheck(Supplier.class, collectorClass);
    if (supplier.isPresent()) {
      List<? extends TypeMirror> typeArgs = asDeclared(supplier.get()).getTypeArguments();
      if (typeArgs.isEmpty()) {
        throw boom("raw Supplier type");
      }
      return collectorType(typeArgs.get(0), true);
    }
    TypeMirror collector = typecheck(Collector.class, collectorClass)
        .orElseThrow(() ->
            boom("not a Collector or Supplier<Collector>"));
    return collectorType(collector, false);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private Optional<TypeMirror> typecheck(Class<?> goal, TypeElement start) {
    return Resolver.typecheck(goal, start, tool());
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the collector class: %s.", message));
  }

  private static final class CollectorType {

    final TypeMirror collectorType; // subtype of Collector
    final boolean supplier; // wrapped in Supplier?

    CollectorType(TypeMirror collectorType, boolean supplier) {
      this.collectorType = collectorType;
      this.supplier = supplier;
    }
  }

  private CollectorType collectorType(TypeMirror collectorType, boolean supplier) {
    if (!tool().isSameErasure(collectorType, Collector.class)) {
      throw boom("must either implement Collector or Supplier<Collector>");
    }
    if (tool().isRawType(collectorType)) {
      throw boom("the collector type must be parameterized");
    }
    return new CollectorType(collectorType, supplier);
  }

  private CustomCollector solve(
      TypeMirror inputType,
      CollectorType collectorType,
      Map<String, TypeMirror> r_result) {
    List<? extends TypeParameterElement> typeParameters = collectorClass.getTypeParameters();
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
    return new CustomCollector(inputType, collectorClass, collectorType.supplier, solution);
  }
}
