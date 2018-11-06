package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.math.BigDecimal;
import java.util.Optional;

class BigDecimalCoercion extends CoercionFactory {

  BigDecimalCoercion() {
    super(BigDecimal.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::new", BigDecimal.class).build());
  }
}
