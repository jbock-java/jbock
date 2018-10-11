package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

class StringCoercion extends CoercionFactory {

  StringCoercion() {
    super(String.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().build();
  }
}
