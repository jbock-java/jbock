package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;

abstract class BasicDoubleCoercion extends BasicNumberCoercion {

  BasicDoubleCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicDoubleCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::valueOf", Double.class));
  }
}
