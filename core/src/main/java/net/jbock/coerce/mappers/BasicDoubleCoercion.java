package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicDoubleCoercion extends BasicNumberCoercion {

  BasicDoubleCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicDoubleCoercion(PrimitiveType trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Double.class).build();
  }
}
