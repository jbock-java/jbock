package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicDoubleCoercion extends BasicNumberCoercion {

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Double.class).build();
  }
}
