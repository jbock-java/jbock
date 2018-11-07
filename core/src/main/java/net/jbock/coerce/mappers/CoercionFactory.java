package net.jbock.coerce.mappers;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.WildcardTypeName;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.OptionalInfo;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;

public abstract class CoercionFactory {

  public boolean handlesOptionalPrimitive() {
    return false;
  }

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

  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.of("$T.requireNonNull($N)", Objects.class, param);
  }

  final TypeMirror mapperReturnType() {
    return mapperReturnType;
  }

  CodeBlock initMapper() {
    return CodeBlock.builder().build();
  }

  TypeMirror paramType() {
    return mapperReturnType;
  }

  public final Coercion getCoercion(
      BasicInfo basicInfo,
      OptionalInfo optionalInfo,
      Optional<TypeMirror> collectorType) {
    return getCoercion(basicInfo, optionalInfo, collectorType, mapExpr(), initMapper());
  }

  private Coercion getCoercion(
      BasicInfo basicInfo,
      OptionalInfo optionalInfo,
      Optional<TypeMirror> collectorType,
      Optional<CodeBlock> mapExpr,
      CodeBlock initMapper) {
    CodeBlock extract;
    TypeMirror paramType;
    if (optionalInfo.optional || basicInfo.repeatable) {
      paramType = basicInfo.returnType;
      ParameterSpec param = ParameterSpec.builder(TypeName.get(paramType), basicInfo.paramName()).build();
      extract = CodeBlock.of("$T.requireNonNull($N)", Objects.class, param);
    } else {
      paramType = paramType();
      ParameterSpec param = ParameterSpec.builder(TypeName.get(paramType), basicInfo.paramName()).build();
      extract = extract(param);
    }
    return Coercion.create(
        collectorParam(basicInfo, mapperReturnType, collectorType),
        mapExpr,
        initMapper,
        mapperReturnType,
        initCollector(collectorType),
        extract,
        paramType,
        basicInfo);
  }

  private Optional<CodeBlock> initCollector(
      Optional<TypeMirror> collectorType) {
    if (!collectorType.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(CodeBlock.of(
        "new $T().get()", collectorType.get()));
  }

  private Optional<ParameterSpec> collectorParam(
      BasicInfo basicInfo,
      TypeMirror mapperReturnType,
      Optional<TypeMirror> collectorType) {
    if (!collectorType.isPresent()) {
      return Optional.empty();
    }
    TypeName t = TypeName.get(mapperReturnType);
    TypeName a = WildcardTypeName.subtypeOf(Object.class);
    TypeName r = TypeName.get(basicInfo.returnType);
    return Optional.of(ParameterSpec.builder(ParameterizedTypeName.get(
        ClassName.get(Collector.class), t, a, r), basicInfo.paramName() + "Collector")
        .build());
  }
}
