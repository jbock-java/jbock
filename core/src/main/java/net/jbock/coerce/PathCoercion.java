package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.nio.file.Path;
import java.nio.file.Paths;

class PathCoercion extends CoercionFactory {

  PathCoercion() {
    super(Path.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::get)", Paths.class).build();
  }
}
