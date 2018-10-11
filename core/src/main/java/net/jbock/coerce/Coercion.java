package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.Objects;
import java.util.Optional;

public final class Coercion {

  private final TypeMirror trigger;

  // only for repeatable
  private final Optional<ParameterSpec> collectorParam;

  // helper.build
  private final CodeBlock map;

  // helper.build
  private final CodeBlock initMapper;

  // helper.build
  private final CodeBlock initCollector;

  // impl constructor
  private final CodeBlock extract;

  // impl constructor
  private final TypeName paramType;

  // impl
  private final FieldSpec field;

  private final CoercionKind kind;

  public Coercion(
      TypeMirror trigger,
      Optional<ParameterSpec> collectorParam,
      CodeBlock map,
      CodeBlock initMapper,
      CodeBlock initCollector,
      CodeBlock extract,
      TypeName paramType,
      FieldSpec field,
      CoercionKind kind) {
    this.trigger = trigger;
    this.collectorParam = collectorParam;
    this.map = map;
    this.initMapper = initMapper;
    this.initCollector = initCollector;
    this.extract = extract;
    this.paramType = paramType;
    this.field = field;
    this.kind = kind;
  }

  /**
   * Maps from String to trigger type
   */
  public CodeBlock map() {
    return map;
  }

  /**
   * Type that triggers this coercion (could be wrapped in Optional or List)
   */
  public TypeName trigger() {
    return TypeName.get(trigger);
  }

  public CodeBlock initMapper() {
    return initMapper;
  }

  public CodeBlock initCollector() {
    return initCollector;
  }

  public TypeName paramType() {
    return paramType;
  }

  public FieldSpec field() {
    return field;
  }

  public CodeBlock extract() {
    return extract;
  }

  Coercion withMapper(CodeBlock map, CodeBlock initMapper) {
    return new Coercion(trigger, collectorParam, map, initMapper, initCollector, extract, paramType, field, kind);
  }

  public Coercion asOptional() {
    TypeName paramType = field.type;
    ParameterSpec param = ParameterSpec.builder(paramType, field.name).build();
    CodeBlock extract = CodeBlock.builder().add("$T.requireNonNull($N)", Objects.class, param).build();
    return new Coercion(trigger, collectorParam, map, initMapper, initCollector, extract, paramType, field, kind);
  }

  public Coercion withCollector() {
    TypeName paramType = field.type;
    CodeBlock extract = CodeBlock.builder()
        .add("$T.requireNonNull($N)", Objects.class, ParameterSpec.builder(paramType, field.name).build())
        .build();
    return new Coercion(trigger, collectorParam, map, initMapper, initCollector, extract, paramType, field, kind);
  }

  public CoercionKind kind() {
    return kind;
  }


  public Optional<ParameterSpec> collectorParam() {
    return collectorParam;
  }

  public CodeBlock collectExpr() {
    if (isDefaultCollector()) {
      return CollectorInfo.standardCollectorInit();
    }
    return CodeBlock.builder().add("$N", collectorParam().get()).build();
  }

  public boolean skipMapCollect() {
    return map.isEmpty() && isDefaultCollector();
  }

  public boolean isDefaultCollector() {
    return initCollector.equals(CollectorInfo.standardCollectorInit());
  }
}
