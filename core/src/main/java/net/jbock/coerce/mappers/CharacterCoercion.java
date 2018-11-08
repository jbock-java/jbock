package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.util.Optional;

final class CharacterCoercion extends CoercionFactory {

  CharacterCoercion() {
    super(Character.class);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("Helper::parseCharacter"));
  }
}
