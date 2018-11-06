package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.nio.charset.Charset;
import java.util.Optional;

class CharsetCoercion extends CoercionFactory {

  CharsetCoercion() {
    super(Charset.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.of("$T::forName", Charset.class));
  }
}
