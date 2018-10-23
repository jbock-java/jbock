package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicCharacterCoercion extends CoercionFactory {

  BasicCharacterCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicCharacterCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map(Helper::parseCharacter)").build();
  }
}
