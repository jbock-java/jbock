package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

public abstract class Coercion {

  /**
   * Maps from String to trigger type
   */
  public abstract CodeBlock map();

  /**
   * Type that triggers this coercion (could be wrapped in Optional or List)
   */
  public abstract TypeName trigger();

  // toString stuff (the gen class overrides toString if possible)
  public final CodeBlock jsonExpr(FieldSpec field) {
    return jsonExpr(field.name);
  }

  // toString stuff
  abstract CodeBlock jsonExpr(String param);

  // toString stuff
  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add("$L -> $L",
        "e", jsonExpr("e")).build();
  }

  /**
   * Specials can't be in Optional or List
   */
  public abstract boolean special();
}
