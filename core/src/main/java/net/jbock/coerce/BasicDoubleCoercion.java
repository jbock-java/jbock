package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicDoubleCoercion extends BasicNumberCoercion {

  BasicDoubleCoercion(Class<?> trigger) {
    super(trigger);
  }

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Double.class).build();
  }
}
