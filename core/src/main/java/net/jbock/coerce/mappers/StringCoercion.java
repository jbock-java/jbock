package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;

class StringCoercion extends CoercionFactory {

  StringCoercion() {
    super(String.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().build();
  }

  @Override
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("quote.apply($L)", param).build();
  }

  CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map(quote)").build();
  }
}
