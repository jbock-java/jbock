package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.time.ZonedDateTime;
import java.util.Optional;

class ZonedDateTimeCoercion extends CoercionFactory {

  ZonedDateTimeCoercion() {
    super(ZonedDateTime.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::parse", ZonedDateTime.class));
  }
}
