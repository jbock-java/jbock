package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicLongCoercion extends BasicNumberCoercion {

  BasicLongCoercion(Class<?> trigger) {
    super(trigger);
  }

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Long.class).build();
  }
}
