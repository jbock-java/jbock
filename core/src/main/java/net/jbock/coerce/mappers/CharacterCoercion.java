package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

final class CharacterCoercion extends SimpleCoercion {

  CharacterCoercion() {
    super(Character.class, CodeBlock.of("Helper::parseCharacter"));
  }

}
