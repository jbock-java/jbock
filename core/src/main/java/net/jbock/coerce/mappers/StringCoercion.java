package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.util.Optional;

class StringCoercion extends CoercionFactory {

  StringCoercion() {
    super(String.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.empty();
  }
}
