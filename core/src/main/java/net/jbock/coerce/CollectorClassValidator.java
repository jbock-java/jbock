package net.jbock.coerce;

import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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

  // visible for testing
  CollectorInfo getCollectorInfo(TypeElement collectorClass) {
    commonChecks(basicInfo, collectorClass, "collector");
    CollectorType collectorType = getCollectorType(collectorClass);
    TypeMirror t = asDeclared(collectorType.type()).getTypeArguments().get(0);
    TypeMirror r = asDeclared(collectorType.type()).getTypeArguments().get(2);
    Map<String, TypeMirror> solution = tool().unify(basicInfo.returnType(), r)
        .orElseThrow(() -> boom(String.format("The collector should return %s but returns %s", basicInfo.returnType(), r)));
    if (tool().substitute(collectorClass.asType(), solution) == null) {
      throw boom("Invalid bounds");
    }
    TypeMirror collectorInput = tool().substitute(t, solution);
    if (collectorInput == null) {
      throw boom("Unexpected: can solve R but not Collector<T, ?, R>");
    }
    return CollectorInfo.create(collectorInput, collectorType);
  }

  private CollectorType getCollectorType(TypeElement collectorClass) {
    Optional<TypeMirror> supplier = Resolver.resolve(
        Supplier.class,
        collectorClass.asType(),
        basicInfo.tool()).resolveTypevars();
    if (supplier.isPresent()) {
      List<? extends TypeMirror> typeArgs = asDeclared(supplier.get()).getTypeArguments();
      if (typeArgs.isEmpty()) {
        throw boom("raw Supplier type");
      }
      return CollectorType.create(basicInfo, typeArgs.get(0), true, collectorClass);
    }
    TypeMirror collector = Resolver.resolve(
        Collector.class,
        collectorClass.asType(),
        basicInfo.tool()).resolveTypevars().orElseThrow(() ->
        boom("not a Collector or Supplier<Collector>"));
    return CollectorType.create(basicInfo, collector, false, collectorClass);
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the collector class: %s.", message));
  }
}
