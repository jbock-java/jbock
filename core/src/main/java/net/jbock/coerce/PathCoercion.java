package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.nio.file.Path;
import java.nio.file.Paths;

class PathCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::get)", Paths.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(Path.class);
  }

  @Override
  public boolean special() {
    return false;
  }
}
