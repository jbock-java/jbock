package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.Objects;

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
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("quote.apply($T.toString($L))", Objects.class, param).build();
  }

  // toString stuff
  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map($T::toString).map(quote)", Objects.class).build();
  }

  /**
   * Specials can't be in Optional or List
   */
  public boolean special() {
    return false;
  }
}
