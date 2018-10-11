package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.nio.file.Path;
import java.nio.file.Paths;

class PathCoercion extends CoercionFactory {

  PathCoercion() {
    super(Path.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::get)", Paths.class).build();
  }
}
