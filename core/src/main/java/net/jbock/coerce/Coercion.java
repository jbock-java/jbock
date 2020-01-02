package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.collectors.AbstractCollector;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.compiler.ParamName;

import java.util.Optional;

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
      CodeBlock extractExpr,
      ParameterSpec constructorParam,
      ParameterStyle style) {
    return getCoercion(basicInfo, collector.collectExpr(),
        mapperType, extractExpr, style, constructorParam);
  }

  public static Coercion getCoercion(
      BasicInfo basicInfo,
      MapperType mapperType,
      CodeBlock extractExpr,
      ParameterSpec constructorParam,
      ParameterStyle style) {
    return getCoercion(basicInfo, CodeBlock.builder().build(),
        mapperType, extractExpr, style, constructorParam);
  }

  private static Coercion getCoercion(
      BasicInfo basicInfo,
      CodeBlock collectExpr,
      MapperType mapperType,
      CodeBlock extractExpr,
      ParameterStyle style,
      ParameterSpec constructorParam) {
    CodeBlock mapExpr = mapperType.mapExpr();
    return new Coercion(collectExpr, mapExpr,
        constructorParam, basicInfo.fieldSpec(), extractExpr, style, basicInfo.parameterName());
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
