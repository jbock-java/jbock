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

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;

import static net.jbock.compiler.Constants.STRING;

public abstract class CoercionFactory {

  /**
   * An expression that maps from String to innerType
   */
  private CodeBlock mapExpr(TypeMirror innerType, String paramName) {
    return CodeBlock.of("$N", mapperParam(innerType, paramName));
  }

  abstract CodeBlock createMapper(TypeMirror innerType);

  final CodeBlock initMapper(TypeMirror innerType, String paramName) {
    ParameterSpec mapperParam = mapperParam(innerType, paramName);
    return CodeBlock.of("$T $N = $L",
        mapperParam.type,
        mapperParam,
        createMapper(innerType));
  }

  private ParameterSpec mapperParam(TypeMirror innerType, String paramName) {
    ParameterizedTypeName mapperParamType = ParameterizedTypeName.get(
        ClassName.get(Function.class), STRING,
        TypeName.get(innerType));
    return ParameterSpec.builder(mapperParamType, paramName + "Mapper").build();
  }

  public final Coercion getCoercion(
      BasicInfo basicInfo,
      Optional<AbstractCollector> collector) {
    TypeMirror innerType = innerType(basicInfo, collector);
    CodeBlock mapExpr = mapExpr(innerType, basicInfo.paramName());
    CodeBlock initMapper = initMapper(innerType, basicInfo.paramName());
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
