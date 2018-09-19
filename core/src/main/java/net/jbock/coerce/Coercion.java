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
  private final TypeName fieldType;

  Coercion(
      TypeName trigger,
      CodeBlock map,
      boolean special,
      Optional<CodeBlock> initMapper,
      TypeName paramType,
      TypeName fieldType) {
    this.trigger = trigger;
    this.map = map;
    this.special = special;
    this.initMapper = initMapper;
    this.paramType = paramType;
    this.fieldType = fieldType;
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
  public final CodeBlock jsonExpr(FieldSpec field) {
    return jsonExpr(field.name);
  }

  // toString stuff
  abstract CodeBlock jsonExpr(String param);

  // toString stuff
  public abstract CodeBlock mapJsonExpr(FieldSpec field);

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
    return trigger;
  }

  public TypeName fieldType() {
    return trigger;
  }
}
