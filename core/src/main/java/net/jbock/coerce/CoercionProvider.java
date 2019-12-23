package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.collectorabsent.mapperabsent.CollectorAbsentMapperAbsent;
import net.jbock.coerce.collectorabsent.mapperpresent.CollectorAbsentMapperPresent;
import net.jbock.coerce.collectorpresent.CollectorClassValidator;
import net.jbock.coerce.collectors.AbstractCollector;
import net.jbock.coerce.collectors.DefaultCollector;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.coerce.mapper.ReferenceMapperType;
import net.jbock.compiler.ParamName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static net.jbock.coerce.ParameterType.FLAG;
import static net.jbock.coerce.ParameterType.REPEATABLE;

public class CoercionProvider {

  private final BasicInfo basicInfo;

  private CoercionProvider(BasicInfo basicInfo) {
    this.basicInfo = basicInfo;
  }

  public static Coercion flagCoercion(ExecutableElement sourceMethod, ParamName paramName) {
    ParameterSpec name = ParameterSpec.builder(TypeName.get(sourceMethod.getReturnType()), paramName.snake()).build();
    return new Coercion(
        CodeBlock.builder().build(),
        CodeBlock.of("$T.identity()", Function.class),
        name,
        FieldSpec.builder(TypeName.get(sourceMethod.getReturnType()), paramName.snake()).build(),
        CodeBlock.of("$N", name),
        FLAG,
        paramName);
  }

  public static Coercion findCoercion(
      ExecutableElement sourceMethod,
      ParamName paramName,
      Optional<TypeElement> mapperClass,
      Optional<TypeElement> collectorClass,
      TypeTool tool) {
    BasicInfo basicInfo = BasicInfo.create(
        mapperClass, collectorClass,
        paramName, sourceMethod, tool);
    return new CoercionProvider(basicInfo).run();
  }

  private Coercion run() {
    if (basicInfo.collectorClass().isPresent()) {
      if (basicInfo.mapperClass().isPresent()) {
        return handleCollectorPresentMapperPresent(basicInfo.mapperClass().get());
      } else {
        return handleCollectorPresentMapperAbsent();
      }
    } else {
      if (basicInfo.mapperClass().isPresent()) {
        return new CollectorAbsentMapperPresent(basicInfo, basicInfo.mapperClass().get()).findCoercion();
      } else {
        return new CollectorAbsentMapperAbsent(basicInfo).findCoercion();
      }
    }
  }

  private Coercion handleCollectorPresentMapperAbsent() {
    AbstractCollector collectorInfo = collectorInfo(Optional.empty());
    CodeBlock mapExpr = basicInfo.findMapExpr(collectorInfo.inputType())
        .orElseThrow(() -> basicInfo.asValidationException(String.format("Unknown parameter type: %s. Try defining a custom mapper.",
            collectorInfo.inputType())));
    MapperType mapperType = MapperType.create(mapExpr);
    Function<ParameterSpec, CodeBlock> extractExpr = p -> CodeBlock.of("$N", p);
    TypeMirror constructorParamType = basicInfo.originalReturnType();
    return Coercion.getCoercion(basicInfo, collectorInfo, mapperType, extractExpr, constructorParamType, REPEATABLE);
  }

  private Coercion handleCollectorPresentMapperPresent(TypeElement mapperClass) {
    AbstractCollector collectorInfo = collectorInfo(Optional.of(mapperClass));
    ReferenceMapperType mapperType = new MapperClassValidator(basicInfo, collectorInfo.inputType(), mapperClass).checkReturnType()
        .orElseThrow(basicInfo::asValidationException);
    Function<ParameterSpec, CodeBlock> extractExpr = p -> CodeBlock.of("$N", p);
    TypeMirror constructorParamType = basicInfo.originalReturnType();
    return Coercion.getCoercion(basicInfo, collectorInfo, mapperType, extractExpr, constructorParamType, REPEATABLE);
  }

  private AbstractCollector collectorInfo(Optional<TypeElement> mapperClass) {
    if (basicInfo.collectorClass().isPresent()) {
      Optional<TypeMirror> mapperPreference = mapperClass.flatMap(clazz -> new MapperPreferenceChecker(basicInfo, clazz).getPreference());
      return new CollectorClassValidator(basicInfo, basicInfo.collectorClass().get(), mapperPreference).getCollectorInfo();
    }
    Optional<TypeMirror> wrapped = tool().unwrap(List.class, basicInfo.originalReturnType());
    if (!wrapped.isPresent()) {
      throw basicInfo.asValidationException("Either define a custom collector, or return List.");
    }
    return new DefaultCollector(wrapped.get());
  }

  private TypeTool tool() {
    return basicInfo.tool();
  }
}
