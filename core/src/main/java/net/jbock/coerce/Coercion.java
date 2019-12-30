package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.collectors.AbstractCollector;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.compiler.ParamName;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

public final class Coercion {

  private final CodeBlock collectExpr;

  private final CodeBlock mapExpr;

  // impl constructor param
  private final ParameterSpec constructorParam;

  // impl field
  private final FieldSpec field;

  private final CodeBlock extractExpr;

  private final ParameterStyle parameterType;

  private final ParamName paramName;

  Coercion(
      CodeBlock collectExpr,
      CodeBlock mapExpr,
      ParameterSpec constructorParam,
      FieldSpec field,
      CodeBlock extractExpr,
      ParameterStyle parameterType,
      ParamName paramName) {
    this.collectExpr = collectExpr;
    this.mapExpr = mapExpr;
    this.constructorParam = constructorParam;
    this.field = field;
    this.extractExpr = extractExpr;
    this.parameterType = parameterType;
    this.paramName = paramName;
  }

  public static Coercion getCoercion(
      BasicInfo basicInfo,
      AbstractCollector collector,
      MapperType mapperType,
      Function<ParameterSpec, CodeBlock> extractExpr,
      TypeMirror constructorParamType,
      ParameterStyle parameterType) {
    return getCoercion(basicInfo, collector.collectExpr(),
        mapperType, extractExpr, constructorParamType, parameterType);
  }

  public static Coercion getCoercion(
      BasicInfo basicInfo,
      MapperType mapperType,
      Function<ParameterSpec, CodeBlock> extractExpr,
      TypeMirror constructorParamType,
      ParameterStyle parameterType) {
    return getCoercion(basicInfo, CodeBlock.builder().build(),
        mapperType, extractExpr, constructorParamType, parameterType);
  }

  private static Coercion getCoercion(
      BasicInfo basicInfo,
      CodeBlock collectExpr,
      MapperType mapperType,
      Function<ParameterSpec, CodeBlock> extractExpr,
      TypeMirror constructorParamType,
      ParameterStyle parameterType) {
    CodeBlock mapExpr = mapperType.mapExpr();
    ParameterSpec constructorParam = ParameterSpec.builder(
        TypeName.get(constructorParamType), basicInfo.paramName()).build();
    return new Coercion(collectExpr, mapExpr,
        constructorParam, basicInfo.fieldSpec(), extractExpr.apply(constructorParam), parameterType, basicInfo.parameterName());
  }

  /**
   * Maps from String to mapperReturnType
   * @return an expression
   */
  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public FieldSpec field() {
    return field;
  }

  public CodeBlock extractExpr() {
    return extractExpr;
  }

  public Optional<CodeBlock> collectExpr() {
    return collectExpr.isEmpty() ? Optional.empty() : Optional.of(collectExpr);
  }

  public boolean isOptional() {
    return parameterType.isOptional();
  }

  public boolean isRepeatable() {
    return parameterType.isRepeatable();
  }

  public ParameterStyle parameterType() {
    return parameterType;
  }

  public ParamName paramName() {
    return paramName;
  }
}
