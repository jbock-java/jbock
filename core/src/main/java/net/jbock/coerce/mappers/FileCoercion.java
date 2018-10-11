package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.io.File;

class FileCoercion extends CoercionFactory {

  FileCoercion() {
    super(File.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::new)", File.class).build();
  }
}
