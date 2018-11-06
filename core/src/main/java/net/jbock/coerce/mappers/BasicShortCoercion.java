package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;

abstract class BasicShortCoercion extends CoercionFactory {

  BasicShortCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicShortCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::valueOf", Short.class));
  }
}
