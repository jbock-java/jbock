package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.net.URI;
import java.util.Optional;

class URICoercion extends CoercionFactory {

  URICoercion() {
    super(URI.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::create", URI.class).build());
  }
}
