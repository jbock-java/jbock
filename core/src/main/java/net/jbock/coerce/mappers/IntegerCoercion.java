package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.util.Optional;

final class IntegerCoercion extends NumberCoercion {

  IntegerCoercion() {
    super(Integer.class);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::valueOf", Integer.class));
  }

}
