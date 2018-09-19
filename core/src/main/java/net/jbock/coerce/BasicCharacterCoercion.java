package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicCharacterCoercion extends CoercionFactory {

  BasicCharacterCoercion(Class<?> trigger) {
    super(trigger);
  }

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().add(".map(Helper::parseCharacter)").build();
  }
}
