package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.time.OffsetDateTime;

class OffsetDateTimeCoercion extends Coercion {

  OffsetDateTimeCoercion() {
    super(OffsetDateTime.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", OffsetDateTime.class).build();
  }
}
