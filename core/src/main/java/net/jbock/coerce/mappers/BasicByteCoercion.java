package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicByteCoercion extends CoercionFactory {

  BasicByteCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicByteCoercion(PrimitiveType trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Byte.class).build();
  }
}
