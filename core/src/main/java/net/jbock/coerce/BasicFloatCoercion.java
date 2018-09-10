package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicFloatCoercion extends BasicNumberCoercion {

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Float.class).build();
  }
}
