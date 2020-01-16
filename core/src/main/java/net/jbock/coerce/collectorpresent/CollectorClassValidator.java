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
import java.util.stream.Collector;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.Util.checkNotAbstract;
import static net.jbock.coerce.reference.ExpectedType.COLLECTOR;

public class CollectorClassValidator {

  private final BasicInfo basicInfo;
  private final TypeElement collectorClass;

  public CollectorClassValidator(BasicInfo basicInfo, TypeElement collectorClass) {
    this.basicInfo = basicInfo;
    this.collectorClass = collectorClass;
  }

  // visible for testing
  public CustomCollector getCollectorInfo() {
    commonChecks(collectorClass);
    checkNotAbstract(collectorClass);
    ReferencedType<Collector> collectorType = new ReferenceTool<>(COLLECTOR, basicInfo, basicInfo.tool(), collectorClass)
        .getReferencedType();
    TypeMirror inputType = collectorType.typeArguments().get(0);
    TypeMirror outputType = collectorType.typeArguments().get(2);
    TypevarMapping rightSolution = tool().unify(basicInfo.originalReturnType(), outputType)
        .orElseThrow(this::boom);
    TypevarMapping leftSolution = TypevarMapping.empty(tool()); // left side is currently ignored
    FlattenerResult result = new Flattener(basicInfo, collectorClass)
        .getTypeParameters(leftSolution, rightSolution)
        .orElseThrow(this::boom);
    return new CustomCollector(tool(), result.substitute(inputType).orElseThrow(f -> boom(f.getMessage())),
        collectorClass, collectorType.isSupplier(), result.getTypeParameters());
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.apply(COLLECTOR.boom(message));
  }
}
