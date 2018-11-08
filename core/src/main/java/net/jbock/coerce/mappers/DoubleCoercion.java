package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.util.Optional;

final class DoubleCoercion extends NumberCoercion {

  DoubleCoercion() {
    super(Double.class);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::valueOf", Double.class));
  }
}
