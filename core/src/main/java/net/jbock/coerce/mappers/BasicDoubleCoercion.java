package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicDoubleCoercion extends BasicNumberCoercion {

  BasicDoubleCoercion(Class<?> trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Double.class).build();
  }
}
