package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.util.Optional;

final class FloatCoercion extends CoercionFactory {

  FloatCoercion() {
    super(Float.class);
  }

  @Override
  final Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::valueOf", Float.class));
  }
}
