package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.nio.charset.Charset;

class CharsetCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::forName)", Charset.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(Charset.class);
  }
}
