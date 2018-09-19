package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.math.BigDecimal;

class BigDecimalCoercion extends CoercionFactory {

  BigDecimalCoercion() {
    super(BigDecimal.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::new)", BigDecimal.class).build();
  }
}
