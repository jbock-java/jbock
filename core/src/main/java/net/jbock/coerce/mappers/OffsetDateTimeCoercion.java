package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.time.OffsetDateTime;

class OffsetDateTimeCoercion extends CoercionFactory {

  OffsetDateTimeCoercion() {
    super(OffsetDateTime.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", OffsetDateTime.class).build();
  }
}
