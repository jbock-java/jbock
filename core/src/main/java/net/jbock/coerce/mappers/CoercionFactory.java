package net.jbock.coerce.mappers;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.stream.Collector;

public abstract class CoercionFactory {

  // trigger for this factory
  abstract TypeMirror mapperReturnType(TypeTool tool);

  /**
   * An expression that maps from String to mapperReturnType
   */
  abstract Optional<CodeBlock> mapExpr(TypeMirror returnType);

  CodeBlock initMapper() {
    return CodeBlock.builder().build();
  }

  public Coercion getCoercion(
      BasicInfo basicInfo,
      Optional<AbstractCollector> collector) {
    TypeMirror innerType = innerType(basicInfo, collector);
    Optional<CodeBlock> mapExpr = mapExpr(innerType);
    CodeBlock initMapper = initMapper();
    TypeMirror constructorParamType = basicInfo.returnType();
    Optional<ParameterSpec> collectorParam = collector.flatMap(collectorInfo ->
        collectorParam(basicInfo, collectorInfo));
    return Coercion.create(
        collectorParam,
        mapExpr,
        initMapper,
        collector,
        constructorParamType,
        basicInfo);
  }

  private TypeMirror innerType(BasicInfo basicInfo, Optional<AbstractCollector> collector) {
    return collector.map(AbstractCollector::inputType).orElse(basicInfo.optionalInfo().orElse(basicInfo.returnType()));
  }

  private static Optional<ParameterSpec> collectorParam(
      BasicInfo basicInfo,
      AbstractCollector collectorInfo) {
    TypeName t = TypeName.get(collectorInfo.inputType());
    TypeName a = WildcardTypeName.subtypeOf(Object.class);
    TypeName r = TypeName.get(basicInfo.returnType());
    return Optional.of(ParameterSpec.builder(ParameterizedTypeName.get(
        ClassName.get(Collector.class), t, a, r),
        basicInfo.paramName() + "Collector")
        .build());
  }
}
