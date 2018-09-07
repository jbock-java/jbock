package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

public abstract class Coercion {

  /**
   * Map from String to trigger
   */
  public abstract CodeBlock map();

  public abstract TypeName trigger();

  public final CodeBlock jsonExpr(FieldSpec field) {
    return jsonExpr(field.name);
  }

  public abstract CodeBlock jsonExpr(String param);

  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add("$N -> $L",
        field, jsonExpr("e")).build();
  }

  /**
   * Specials can't be in Optional or List
   */
  public abstract boolean special();
}
