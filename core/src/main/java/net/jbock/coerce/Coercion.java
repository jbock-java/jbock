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
import java.util.Optional;

public final class Coercion {

  private static final ClassName BOOLEAN_CLASS = ClassName.get(Boolean.class);

  private final TypeName trigger;
  private final CodeBlock map;
  private final boolean special;
  private final CodeBlock initMapper;
  private final CodeBlock jsonExpr;
  private final CodeBlock mapJsonExpr;
  private final CodeBlock extract;
  private final TypeName paramType;
  private final FieldSpec field;
  private final CoercionKind kind;

  Coercion(
      TypeName trigger,
      CodeBlock map,
      boolean special,
      CodeBlock initMapper,
      CodeBlock jsonExpr,
      CodeBlock mapJsonExpr,
      CodeBlock extract,
      TypeName paramType,
      FieldSpec field,
      CoercionKind kind) {
    this.trigger = trigger;
    this.map = map;
    this.special = special;
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

  /**
   * Specials can't be in Optional or List
   */
  public boolean special() {
    return special;
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
    return field.type.equals(TypeName.BOOLEAN) || BOOLEAN_CLASS.equals(field.type);
  }

  public boolean repeatable() {
    return kind == CoercionKind.LIST_COMBINATION;
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

  Coercion withMapper(CodeBlock map, CodeBlock initMapper) {
    return new Coercion(trigger, map, special, initMapper, jsonExpr, mapJsonExpr, extract, paramType, field, kind);
  }

  Coercion asOptional() {
    TypeName paramType = Util.optionalOf(this.paramType);
    CodeBlock extract = CodeBlock.builder()
        .add("$T.requireNonNull($N)", Objects.class, ParameterSpec.builder(paramType, field.name).build())
        .build();
    return new Coercion(trigger, map, special, initMapper, jsonExpr, mapJsonExpr, extract, paramType, field, kind);
  }

  Coercion asList() {
    TypeName paramType = Util.listOf(this.paramType);
    CodeBlock extract = CodeBlock.builder()
        .add("$T.unmodifiableList($N)", Collections.class, ParameterSpec.builder(paramType, field.name).build())
        .build();
    return new Coercion(trigger, map, special, initMapper, jsonExpr, mapJsonExpr, extract, paramType, field, kind);
  }
}
