package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicIntegerCoercion extends BasicNumberCoercion {

  BasicIntegerCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicIntegerCoercion(PrimitiveType trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Integer.class).build();
  }

}
