package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.time.Instant;

class InstantCoercion extends CoercionFactory {

  InstantCoercion() {
    super(Instant.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", Instant.class).build();
  }
}
