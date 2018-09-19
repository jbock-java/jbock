package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.time.ZonedDateTime;

class ZonedDateTimeCoercion extends Coercion {

  ZonedDateTimeCoercion() {
    super(ZonedDateTime.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", ZonedDateTime.class).build();
  }
}
