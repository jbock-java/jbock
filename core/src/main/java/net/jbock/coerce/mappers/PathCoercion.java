package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.nio.file.Path;
import java.nio.file.Paths;

class PathCoercion extends SimpleCoercion {

  PathCoercion() {
    super(Path.class, CodeBlock.of("$T::get", Paths.class));
  }

}
