package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;

abstract class BasicFloatCoercion extends BasicNumberCoercion {

  BasicFloatCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicFloatCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::valueOf", Float.class));
  }
}
