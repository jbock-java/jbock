package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.time.LocalDateTime;

class LocalDateTimeCoercion extends Coercion {

  LocalDateTimeCoercion() {
    super(LocalDateTime.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", LocalDateTime.class).build();
  }
}
