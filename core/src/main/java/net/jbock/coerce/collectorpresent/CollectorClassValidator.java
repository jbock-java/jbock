package net.jbock.coerce.collectorpresent;

import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Flattener;
import net.jbock.coerce.FlattenerResult;
import net.jbock.coerce.collectors.CustomCollector;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.coerce.reference.ReferencedType;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.TypevarMapping;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
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
    TypevarMapping t_result = TypevarMapping.empty(tool());
    FlattenerResult typeParameters = new Flattener(basicInfo, collectorClass)
        .getTypeParameters(t_result, r_result)
        .orElseThrow(this::boom);
    TypeMirror inputType = typeParameters.substitute(t).orElseThrow(f -> boom(f.getMessage()));
    return new CustomCollector(tool(), inputType, collectorClass, collectorType.isSupplier(), typeParameters.getTypeParameters());
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(COLLECTOR.boom(message));
  }
}
