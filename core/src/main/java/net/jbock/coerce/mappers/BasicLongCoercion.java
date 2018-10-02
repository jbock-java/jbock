package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicLongCoercion extends BasicNumberCoercion {

  BasicLongCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicLongCoercion(PrimitiveType trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Long.class).build();
  }
}
