package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicShortCoercion extends CoercionFactory {

  BasicShortCoercion(Class<?> trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Short.class).build();
  }
}
