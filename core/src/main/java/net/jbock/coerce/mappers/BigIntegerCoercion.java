package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.math.BigInteger;
import java.util.Optional;

class BigIntegerCoercion extends CoercionFactory {

  BigIntegerCoercion() {
    super(BigInteger.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::new", BigInteger.class));
  }
}
