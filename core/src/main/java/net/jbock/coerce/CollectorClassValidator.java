package net.jbock.coerce;

import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.compiler.TypeTool.asDeclared;

class CollectorClassValidator {

  // visible for testing
  static CollectorInfo getCollectorInfo(
      TypeElement collectorClass,
      BasicInfo basicInfo) {
    commonChecks(basicInfo, collectorClass, "collector");
    TypeMirror collectorType = getCollectorType(collectorClass, basicInfo);
    TypeMirror t = asDeclared(collectorType).getTypeArguments().get(0);
    TypeMirror r = asDeclared(collectorType).getTypeArguments().get(2);
    Map<String, TypeMirror> solution = basicInfo.tool().unify(basicInfo.returnType(), r)
        .orElseThrow(() -> boom(basicInfo, String.format("The collector should return %s but returns %s", basicInfo.returnType(), r)));
    TypeMirror collectorClassSolved = basicInfo.tool().substitute(collectorClass.asType(), solution);
    if (collectorClassSolved == null) {
      throw boom(basicInfo, "Invalid bounds");
    }
    TypeMirror substitute = basicInfo.tool().substitute(t, solution);
    if (substitute == null) {
      throw boom(basicInfo, "Unexpected: can solve R but not Collector<T, ?, R>");
    }
    return CollectorInfo.create(substitute, collectorClassSolved);
  }

  private static TypeMirror getCollectorType(TypeElement collectorClass, BasicInfo basicInfo) {
    Resolver resolver = Resolver.resolve(basicInfo.tool().asType(Supplier.class), collectorClass.asType(), basicInfo.tool());
    TypeMirror typeMirror = resolver.resolveTypevars().orElseThrow(() -> boom(basicInfo, "not a Supplier"));
    if (basicInfo.tool().isRawType(typeMirror)) {
      throw boom(basicInfo, "the supplier must be parameterized");
    }
    TypeMirror collectorType = asDeclared(typeMirror).getTypeArguments().get(0);
    if (!basicInfo.tool().isSameErasure(collectorType, Collector.class)) {
      throw boom(basicInfo, "the supplier must supply a Collector");
    }
    if (basicInfo.tool().isRawType(collectorType)) {
      throw boom(basicInfo, "the collector type must be parameterized");
    }
    return collectorType;
  }

  private static ValidationException boom(BasicInfo basicInfo, String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the collector class: %s.", message));
  }
}
