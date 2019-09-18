package net.jbock.coerce;

import net.jbock.coerce.collector.CustomCollector;
import net.jbock.coerce.reference.AbstractReferencedType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.ValidationException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.reference.ReferenceTool.Name.MAPPER;

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
    AbstractReferencedType collectorType = new ReferenceTool(MAPPER, basicInfo, collectorClass, Collector.class)
        .getReferencedType();
    TypeMirror t = collectorType.expectedType.getTypeArguments().get(0);
    TypeMirror r = collectorType.expectedType.getTypeArguments().get(2);
    Map<String, TypeMirror> r_result = tool().unify(basicInfo.returnType(), r)
        .orElseThrow(() -> boom(String.format("The collector should return %s but returns %s", basicInfo.returnType(), r)));
    TypeMirror inputType = tool().substitute(t, r_result);
    if (inputType == null) {
      throw boom("could not resolve all type parameters");
    }
    return solve(inputType, collectorType, collectorType.mapTypevars(r_result));
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }

  private ValidationException boom(String message) {
    return basicInfo.asValidationException(String.format("There is a problem with the collector class: %s.", message));
  }

  private CustomCollector solve(
      TypeMirror inputType,
      AbstractReferencedType collectorType,
      Map<String, TypeMirror> r_result) {
    List<? extends TypeParameterElement> typeParameters = collectorClass.getTypeParameters();
    List<TypeMirror> solution = new ArrayList<>(typeParameters.size());
    for (TypeParameterElement typeParameter : typeParameters) {
      String param = typeParameter.toString();
      TypeMirror rMirror = r_result.get(param);
      TypeMirror s = null;
      if (rMirror != null) {
        if (tool().isOutOfBounds(rMirror, typeParameter.getBounds())) {
          throw boom("invalid bounds");
        }
        s = rMirror;
      }
      solution.add(s);
    }
    return new CustomCollector(inputType, collectorClass, collectorType.isSupplier(), solution);
  }
}
