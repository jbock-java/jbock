package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicIntegerCoercion extends Coercion {

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Integer.class).build();
  }
}
