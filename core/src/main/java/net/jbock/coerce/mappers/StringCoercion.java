package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

class StringCoercion extends CoercionFactory {

  StringCoercion() {
    super(String.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().build();
  }
}
