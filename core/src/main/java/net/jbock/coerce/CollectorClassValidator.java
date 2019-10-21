package net.jbock.coerce;

import net.jbock.coerce.collector.CustomCollector;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Left;
import net.jbock.coerce.either.Right;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.reference.ExpectedType.COLLECTOR;

class CollectorClassValidator {

  private final BasicInfo basicInfo;
  private final TypeElement collectorClass;

  CollectorClassValidator(BasicInfo basicInfo, TypeElement collectorClass) {
    this.basicInfo = basicInfo;
    this.collectorClass = collectorClass;
  }

  // visible for testing
  CustomCollector getCollectorInfo() {
    commonChecks(basicInfo, collectorClass, "collector");
    ReferencedType<Collector> collectorType = new ReferenceTool<>(COLLECTOR, basicInfo, collectorClass)
        .getReferencedType();
    TypeMirror t = collectorType.expectedType().typeArguments().get(0);
    TypeMirror r = collectorType.expectedType().typeArguments().get(2);
    Map<String, TypeMirror> r_result = tool().unify(basicInfo.originalReturnType(), r)
        .orElseThrow(() -> boom(String.format("The collector should return %s but returns %s", basicInfo.originalReturnType(), r)));
    TypeMirror inputType = tool().substitute(t, r_result);
    if (inputType == null) {
      throw boom("could not resolve all type parameters");
    }
    Either<List<TypeMirror>, String> typeParameters = new Flattener(basicInfo, collectorClass).getTypeParameters(r_result);
    if (typeParameters instanceof Right) {
      throw boom(((Right<List<TypeMirror>, String>) typeParameters).value());
    }
    return new CustomCollector(tool(), inputType, collectorClass, collectorType.isSupplier(),
        ((Left<List<TypeMirror>, String>) typeParameters).value());
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return COLLECTOR.boom(basicInfo, message);
  }
}
