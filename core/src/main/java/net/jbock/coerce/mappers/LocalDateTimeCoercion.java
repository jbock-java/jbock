package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.time.LocalDateTime;
import java.util.Optional;

class LocalDateTimeCoercion extends CoercionFactory {

  LocalDateTimeCoercion() {
    super(LocalDateTime.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::parse", LocalDateTime.class).build());
  }
}
