package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;

abstract class BasicCharacterCoercion extends CoercionFactory {

  BasicCharacterCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicCharacterCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("Helper::parseCharacter"));
  }
}
