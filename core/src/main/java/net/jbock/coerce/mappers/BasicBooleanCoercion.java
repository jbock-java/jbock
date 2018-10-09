package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicBooleanCoercion extends CoercionFactory {

  BasicBooleanCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicBooleanCoercion(PrimitiveType trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Boolean.class).build();
  }
}
