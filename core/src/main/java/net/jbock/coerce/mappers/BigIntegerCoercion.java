package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.math.BigInteger;

class BigIntegerCoercion extends CoercionFactory {

  BigIntegerCoercion() {
    super(BigInteger.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::new)", BigInteger.class).build();
  }
}
