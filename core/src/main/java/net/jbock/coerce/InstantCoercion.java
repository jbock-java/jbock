package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.time.Instant;

class InstantCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::parse)", Instant.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(Instant.class);
  }
}
