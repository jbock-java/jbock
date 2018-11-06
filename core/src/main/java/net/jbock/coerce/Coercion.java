package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public final class Coercion {

  // only for repeatable
  private final Optional<ParameterSpec> collectorParam;

  // helper.build
  private final Optional<CodeBlock> mapExpr;

  // helper.build
  private final CodeBlock initMapper;

  // helper.build
  private final CodeBlock initCollector;

  // impl constructor
  private final CodeBlock extract;

  // impl constructor
  private final TypeMirror paramType;

  // impl
  private final FieldSpec field;

  private Coercion(
      Optional<ParameterSpec> collectorParam,
      Optional<CodeBlock> mapExpr,
      CodeBlock initMapper,
      CodeBlock initCollector,
      CodeBlock extract,
      TypeMirror paramType,
      FieldSpec field) {
    this.collectorParam = collectorParam;
    this.mapExpr = mapExpr;
    this.initMapper = initMapper;
    this.initCollector = initCollector;
    this.extract = extract;
    this.paramType = paramType;
    this.field = field;
  }

  public static Coercion create(
      Optional<ParameterSpec> collectorParam,
      Optional<CodeBlock> mapExpr,
      CodeBlock initMapper,
      CodeBlock initCollector,
      CodeBlock extract,
      TypeMirror paramType,
      BasicInfo basicInfo) {
    return new Coercion(collectorParam, mapExpr, initMapper, initCollector, extract, paramType, basicInfo.fieldSpec());
  }

  /**
   * Maps from String to mapperReturnType
   */
  public Optional<CodeBlock> mapExpr() {
    return mapExpr;
  }

  public CodeBlock initMapper() {
    return initMapper;
  }

  public CodeBlock initCollector() {
    return initCollector;
  }

  public TypeName paramType() {
    return TypeName.get(paramType);
  }

  public FieldSpec field() {
    return field;
  }

  public CodeBlock extract() {
    return extract;
  }


  public Optional<ParameterSpec> collectorParam() {
    return collectorParam;
  }

  public Optional<CodeBlock> collectExpr() {
    if (isDefaultCollector()) {
      return Optional.of(CollectorInfo.standardCollectorInit());
    }
    if (!collectorParam.isPresent()) {
      return Optional.empty();
    }
    return Optional.of(CodeBlock.builder().add("$N", collectorParam.get()).build());
  }

  public boolean skipMapCollect() {
    return !mapExpr.isPresent() && isDefaultCollector();
  }

  public boolean isDefaultCollector() {
    return initCollector.equals(CollectorInfo.standardCollectorInit());
  }
}
