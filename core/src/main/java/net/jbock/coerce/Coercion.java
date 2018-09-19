package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Optional;

public final class Coercion {

  private final TypeName trigger;
  private final CodeBlock map;
  private final boolean special;
  private final Optional<CodeBlock> initMapper;
  private final CodeBlock jsonExpr;
  private final CodeBlock mapJsonExpr;
  private final TypeName paramType;
  private final FieldSpec field;

  Coercion(
      TypeName trigger,
      CodeBlock map,
      boolean special,
      Optional<CodeBlock> initMapper,
      CodeBlock jsonExpr,
      CodeBlock mapJsonExpr,
      TypeName paramType,
      FieldSpec field) {
    this.trigger = trigger;
    this.map = map;
    this.special = special;
    this.initMapper = initMapper;
    this.jsonExpr = jsonExpr;
    this.mapJsonExpr = mapJsonExpr;
    this.paramType = paramType;
    this.field = field;
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

  public Optional<CodeBlock> initMapper() {
    return initMapper;
  }

  public TypeName paramType() {
    return paramType;
  }

  public FieldSpec field() {
    return field;
  }
}
