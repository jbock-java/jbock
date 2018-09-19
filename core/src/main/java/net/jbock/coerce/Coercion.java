package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Optional;

public abstract class Coercion {

  private final TypeName trigger;
  private final CodeBlock map;
  private final boolean special;
  private final Optional<CodeBlock> initMapper;
  private final TypeName paramType;
  private final FieldSpec field;

  Coercion(
      TypeName trigger,
      CodeBlock map,
      boolean special,
      Optional<CodeBlock> initMapper,
      TypeName paramType,
      FieldSpec field) {
    this.trigger = trigger;
    this.map = map;
    this.special = special;
    this.initMapper = initMapper;
    this.paramType = paramType;
    this.field = field;
  }

  /**
   * Maps from String to trigger type
   */
  public final CodeBlock map() {
    return map;
  }

  /**
   * Type that triggers this coercion (could be wrapped in Optional or List)
   */
  public final TypeName trigger() {
    return trigger;
  }

  // toString stuff (the gen class overrides toString if possible)
  public final CodeBlock jsonExpr(FieldSpec field) {
    return jsonExpr(field.name);
  }

  // toString stuff
  abstract CodeBlock jsonExpr(String param);

  // toString stuff
  public abstract CodeBlock mapJsonExpr();

  /**
   * Specials can't be in Optional or List
   */
  public final boolean special() {
    return special;
  }

  public final Optional<CodeBlock> initMapper() {
    return initMapper;
  }

  public final TypeName paramType() {
    return trigger;
  }

  public final TypeName fieldType() {
    return field.type;
  }

  public final FieldSpec field() {
    return field;
  }
}
