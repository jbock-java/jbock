package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.mapper.MapperType;
import net.jbock.compiler.ParamName;

import java.util.Optional;

/**
 * The result of coercion: Information about a single parameter (option or param).
 */
public final class Coercion {

  private final CodeBlock collectExpr;

  private final CodeBlock mapExpr;

  // impl constructor param
  private final ParameterSpec constructorParam;

  // impl field
  private final FieldSpec field;

  private final CodeBlock extractExpr;

  // absent -> flag
  private final Optional<ParameterStyle> parameterType;

  private final ParamName paramName;

  Coercion(
      CodeBlock collectExpr,
      CodeBlock mapExpr,
      ParameterSpec constructorParam,
      FieldSpec field,
      CodeBlock extractExpr,
      Optional<ParameterStyle> parameterType,
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
      CodeBlock collectExpr,
      MapperType mapperType,
      CodeBlock extractExpr,
      ParameterStyle style,
      ParameterSpec constructorParam) {
    CodeBlock mapExpr = mapperType.mapExpr();
    return new Coercion(collectExpr, mapExpr,
        constructorParam, basicInfo.fieldSpec(), extractExpr, Optional.of(style), basicInfo.parameterName());
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

  public CodeBlock collectExpr() {
    return collectExpr;
  }

  public Optional<ParameterStyle> getStyle() {
    return parameterType;
  }

  public ParamName paramName() {
    return paramName;
  }
}
