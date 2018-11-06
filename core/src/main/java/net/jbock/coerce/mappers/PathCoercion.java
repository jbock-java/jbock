package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

class PathCoercion extends CoercionFactory {

  PathCoercion() {
    super(Path.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::get", Paths.class).build());
  }
}
