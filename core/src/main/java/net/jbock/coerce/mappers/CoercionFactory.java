package net.jbock.coerce.mappers;

import com.squareup.javapoet.*;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.stream.Collector;

public abstract class CoercionFactory {

  final TypeMirror mapperReturnType;

  CoercionFactory(Class<?> mapperReturnType) {
    this(TypeTool.get().getTypeElement(mapperReturnType).asType());
  }

  CoercionFactory(TypeMirror mapperReturnType) {
    this.mapperReturnType = mapperReturnType;
  }

  /**
   * An expression that maps from String to mapperReturnType
   */
  abstract Optional<CodeBlock> mapExpr();

  final TypeMirror mapperReturnType() {
    return mapperReturnType;
  }

  CodeBlock initMapper() {
    return CodeBlock.builder().build();
  }

  public final Coercion getCoercion(
      BasicInfo basicInfo,
      Optional<TypeMirror> collectorType) {
    return getCoercion(basicInfo, collectorType, mapExpr(), initMapper());
  }

  private Coercion getCoercion(
      BasicInfo basicInfo,
      Optional<TypeMirror> collectorType,
      Optional<CodeBlock> mapExpr,
      CodeBlock initMapper) {
    TypeMirror constructorParamType = getConstructorParamType(basicInfo);
    Optional<ParameterSpec> collectorParam;
    if (!collectorType.isPresent()) {
      collectorParam = Optional.empty();
    } else {
      collectorParam = collectorParam(basicInfo, mapperReturnType);
    }
    return Coercion.create(
        collectorParam,
        mapExpr,
        initMapper,
        mapperReturnType,
        collectorType.map(type -> CodeBlock.of("new $T().get()", type)),
        constructorParamType,
        basicInfo);
  }

  private TypeMirror getConstructorParamType(BasicInfo basicInfo) {
    boolean useReturnType = basicInfo.optionalInfo().isPresent() || basicInfo.repeatable;
    if (useReturnType) {
      return basicInfo.returnType();
    }
    return mapperReturnType;
  }

  private Optional<ParameterSpec> collectorParam(
      BasicInfo basicInfo,
      TypeMirror mapperReturnType) {
    TypeName t = TypeName.get(mapperReturnType);
    TypeName a = WildcardTypeName.subtypeOf(Object.class);
    TypeName r = TypeName.get(basicInfo.returnType());
    return Optional.of(ParameterSpec.builder(ParameterizedTypeName.get(
        ClassName.get(Collector.class), t, a, r), basicInfo.paramName() + "Collector")
        .build());
  }
}
