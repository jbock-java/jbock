package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicCharacterCoercion extends CoercionFactory {

  BasicCharacterCoercion(Class<?> trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map(Helper::parseCharacter)").build();
  }
}
