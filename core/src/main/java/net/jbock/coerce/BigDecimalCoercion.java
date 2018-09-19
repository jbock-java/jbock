package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.math.BigDecimal;

class BigDecimalCoercion extends Coercion {

  BigDecimalCoercion() {
    super(BigDecimal.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::new)", BigDecimal.class).build();
  }
}
