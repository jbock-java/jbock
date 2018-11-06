package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.time.Instant;
import java.util.Optional;

class InstantCoercion extends CoercionFactory {

  InstantCoercion() {
    super(Instant.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::parse", Instant.class));
  }
}
