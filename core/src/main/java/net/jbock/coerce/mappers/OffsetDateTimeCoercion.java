package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.time.OffsetDateTime;
import java.util.Optional;

class OffsetDateTimeCoercion extends CoercionFactory {

  OffsetDateTimeCoercion() {
    super(OffsetDateTime.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::parse", OffsetDateTime.class).build());
  }
}
