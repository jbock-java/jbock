package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;

abstract class BasicBooleanCoercion extends CoercionFactory {

  BasicBooleanCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicBooleanCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::valueOf", Boolean.class).build());
  }
}
