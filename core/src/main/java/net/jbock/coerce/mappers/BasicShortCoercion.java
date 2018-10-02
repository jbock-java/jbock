package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicShortCoercion extends CoercionFactory {

  BasicShortCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicShortCoercion(PrimitiveType trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Short.class).build();
  }
}
