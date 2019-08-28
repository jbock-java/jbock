package net.jbock.coerce.mappers;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CollectorType;
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

  public Coercion getCoercion(BasicInfo basicInfo, Optional<CollectorType> collectorType) {
    Optional<CodeBlock> mapExpr = mapExpr();
    CodeBlock initMapper = initMapper();
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
        collectorType.map(type -> CodeBlock.of(type.supplier() ?
            "new $T().get()" :
            "new $T()",
            type.collectorType())),
        constructorParamType,
        basicInfo);
  }

  private TypeMirror getConstructorParamType(BasicInfo basicInfo) {
    boolean useReturnType = basicInfo.isOptional() || basicInfo.isRepeatable();
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
