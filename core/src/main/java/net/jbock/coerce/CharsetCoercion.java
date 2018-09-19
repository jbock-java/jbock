package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

import java.nio.charset.Charset;

class CharsetCoercion extends CoercionFactory {

  CharsetCoercion() {
    super(Charset.class);
  }

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::forName)", Charset.class).build();
  }
}
