package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.time.Instant;

class InstantCoercion extends Coercion {

  InstantCoercion() {
    super(Instant.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", Instant.class).build();
  }
}
