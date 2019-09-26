package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.collector.AbstractCollector;
import net.jbock.coerce.mapper.MapperType;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.function.Function;

public final class Coercion {

  private final Optional<CodeBlock> collectExpr;

  // helper.build
  private final CodeBlock mapExpr;

  // impl constructor param
  private final ParameterSpec constructorParam;

  // impl
  private final FieldSpec field;

  private final CodeBlock extractExpr;

  private final ParameterType parameterType;

  Coercion(
      Optional<CodeBlock> collectExpr,
      CodeBlock mapExpr,
      ParameterSpec constructorParam,
      FieldSpec field,
      CodeBlock extractExpr,
      ParameterType parameterType) {
    this.collectExpr = collectExpr;
    this.mapExpr = mapExpr;
    this.constructorParam = constructorParam;
    this.field = field;
    this.extractExpr = extractExpr;
    this.parameterType = parameterType;
  }

  static Coercion getCoercion(
      BasicInfo basicInfo,
      Optional<AbstractCollector> collector,
      MapperType mapperType,
      Function<ParameterSpec, CodeBlock> extractExpr,
      TypeMirror constructorParamType,
      ParameterType parameterType) {
    if (collector.isPresent() && !parameterType.repeatable()) {
      throw new AssertionError();
    }
    CodeBlock mapExpr = mapperType.mapExpr();
    ParameterSpec constructorParam = ParameterSpec.builder(
        TypeName.get(constructorParamType), basicInfo.paramName()).build();
    Optional<CodeBlock> collectorInfo = collector.map(AbstractCollector::createCollector);
    return new Coercion(collectorInfo, mapExpr,
        constructorParam, basicInfo.fieldSpec(), extractExpr.apply(constructorParam), parameterType);
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
    return collectExpr;
  }

  public boolean optional() {
    return parameterType.optional();
  }

  public boolean repeatable() {
    return parameterType.repeatable();
  }

  public ParameterType parameterType() {
    return parameterType;
  }
}
