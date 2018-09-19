package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.io.File;

class FileCoercion extends CoercionFactory {

  FileCoercion() {
    super(File.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::new)", File.class).build();
  }
}
