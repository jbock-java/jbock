package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.io.File;
import java.util.Optional;

class FileCoercion extends CoercionFactory {

  FileCoercion() {
    super(File.class);
  }

  @Override
  Optional<CodeBlock> mapExpr() {
    return Optional.of(CodeBlock.builder().add("$T::new", File.class).build());
  }
}
