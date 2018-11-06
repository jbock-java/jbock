package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;

abstract class BasicByteCoercion extends CoercionFactory {

  BasicByteCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicByteCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::valueOf", Byte.class).build());
  }
}
