package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.util.Optional;
import java.util.regex.Pattern;

class PatternCoercion extends CoercionFactory {

  PatternCoercion() {
    super(Pattern.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::compile", Pattern.class).build());
  }
}
