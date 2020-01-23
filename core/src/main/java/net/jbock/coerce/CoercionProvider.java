package net.jbock.coerce;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.collectorabsent.CollectorAbsentAuto;
import net.jbock.coerce.collectorabsent.CollectorAbsentExplicit;
import net.jbock.compiler.ParamName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.coerce.NonFlagSkew.REPEATABLE;

public class CoercionProvider {

  private final BasicInfo basicInfo;

  private CoercionProvider(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  public static Coercion nonFlagCoercion(
      ExecutableElement sourceMethod,
      ParamName paramName,
      Optional<TypeElement> mapperClass,
      Optional<TypeElement> collectorClass,
      ClassName optionType,
      TypeTool tool) {
    BasicInfo basicInfo = BasicInfo.create(
        mapperClass, collectorClass,
        paramName, optionType, sourceMethod, tool);
    return new CoercionProvider(basicInfo).findCoercion();
  }

  private Coercion findCoercion() {
    if (basicInfo.collectorClass().isPresent()) {
      CollectorInfo collectorInfo = new CollectorClassValidator(basicInfo::failure,
          basicInfo.tool(), basicInfo.collectorClass().get(),
          basicInfo.originalReturnType()).getCollectorInfo();
      ParameterSpec constructorParam = basicInfo.constructorParam(basicInfo.originalReturnType());
      TypeMirror inputType = collectorInfo.inputType();
      CodeBlock mapExpr = basicInfo.mapperClass()
          .map(mapperClass -> collectorPresentExplicit(inputType, mapperClass))
          .orElseGet(() -> collectorPresentAuto(inputType));
      return new NonFlagCoercion(basicInfo, collectorInfo.collectExpr(), mapExpr,
          CodeBlock.of("$N", constructorParam), REPEATABLE, constructorParam);
    }
    if (basicInfo.mapperClass().isPresent()) {
      return new CollectorAbsentExplicit(basicInfo, basicInfo.mapperClass().get()).findCoercion();
    } else {
      return new CollectorAbsentAuto(basicInfo).findCoercion();
    }
  }

  private CodeBlock collectorPresentAuto(TypeMirror inputType) {
    return basicInfo.findAutoMapper(inputType)
        .orElseThrow(() -> basicInfo.failure(String.format("Unknown parameter type: %s. Try defining a custom mapper.",
            inputType)));
  }

  private CodeBlock collectorPresentExplicit(TypeMirror inputType, TypeElement mapperClass) {
    return new MapperClassValidator(basicInfo::failure, basicInfo.tool(), inputType, mapperClass).getMapExpr()
        .orElseThrow(basicInfo::failure);
  }
}
