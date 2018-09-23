package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Util;

import java.util.Collections;
import java.util.Objects;

public final class Coercion {

  private static final ClassName BOOLEAN_CLASS = ClassName.get(Boolean.class);

  private final TypeName trigger;

  // helper.build
  private final CodeBlock map;

  // helper.build
  private final CodeBlock initMapper;

  // toString
  private final CodeBlock jsonExpr;

  // toString
  private final CodeBlock mapJsonExpr;

  // impl constructor
  private final CodeBlock extract;

  // impl constructor
  private final TypeName paramType;

  // impl
  private final FieldSpec field;

  private final CoercionKind kind;

  public Coercion(
      TypeName trigger,
      CodeBlock map,
      CodeBlock initMapper,
      CodeBlock jsonExpr,
      CodeBlock mapJsonExpr,
      CodeBlock extract,
      TypeName paramType,
      FieldSpec field,
      CoercionKind kind) {
    this.trigger = trigger;
    this.map = map;
    this.initMapper = initMapper;
    this.jsonExpr = jsonExpr;
    this.mapJsonExpr = mapJsonExpr;
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
    return trigger;
  }

  // toString stuff (the gen class overrides toString if possible)
  public CodeBlock jsonExpr() {
    return jsonExpr;
  }

  // toString stuff
  public CodeBlock mapJsonExpr() {
    return mapJsonExpr;
  }

  public CodeBlock initMapper() {
    return initMapper;
  }

  public TypeName paramType() {
    return paramType;
  }

  public FieldSpec field() {
    return field;
  }

  public boolean flag() {
    return initMapper.isEmpty() &&
        (TypeName.BOOLEAN.equals(field.type) || BOOLEAN_CLASS.equals(field.type));
  }

  public CodeBlock extract() {
    return extract;
  }

  public boolean required() {
    if (flag()) {
      return false;
    }
    if (field.type.equals(Constants.OPTIONAL_INT) ||
        field.type.equals(Constants.OPTIONAL_DOUBLE) ||
        field.type.equals(Constants.OPTIONAL_LONG)) {
      return false;
    }
    return kind == CoercionKind.SIMPLE;
  }

  public Coercion withMapper(CodeBlock map, CodeBlock initMapper) {
    return new Coercion(trigger, map, initMapper, jsonExpr, mapJsonExpr, extract, paramType, field, kind);
  }

  public Coercion asOptional() {
    TypeName paramType = Util.optionalOf(this.paramType);
    CodeBlock extract = CodeBlock.builder()
        .add("$T.requireNonNull($N)", Objects.class, ParameterSpec.builder(paramType, field.name).build())
        .build();
    return new Coercion(trigger, map, initMapper, jsonExpr, mapJsonExpr, extract, paramType, field, kind);
  }

  public Coercion asList() {
    TypeName paramType = Util.listOf(this.paramType);
    CodeBlock extract = CodeBlock.builder()
        .add("$T.unmodifiableList($N)", Collections.class, ParameterSpec.builder(paramType, field.name).build())
        .build();
    return new Coercion(trigger, map, initMapper, jsonExpr, mapJsonExpr, extract, paramType, field, kind);
  }

  public CoercionKind kind() {
    return kind;
  }
}
