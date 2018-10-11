package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicCharacterCoercion extends CoercionFactory {

  BasicCharacterCoercion(Class<?> trigger) {
    super(trigger);
  }

  BasicCharacterCoercion(PrimitiveType trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map(Helper::parseCharacter)").build();
  }
}
