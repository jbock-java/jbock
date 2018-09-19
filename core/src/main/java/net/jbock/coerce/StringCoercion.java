package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;

class StringCoercion extends Coercion {

  StringCoercion() {
    super(String.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().build();
  }

  @Override
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("quote.apply($L)", param).build();
  }

  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map(quote)").build();
  }
}
