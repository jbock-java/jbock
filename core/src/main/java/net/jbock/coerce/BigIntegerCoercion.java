package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.math.BigInteger;

class BigIntegerCoercion extends CoercionFactory {

  BigIntegerCoercion() {
    super(BigInteger.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::new)", BigInteger.class).build();
  }
}
