package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import java.net.URI;

class URICoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().add(".map($T::create)", URI.class).build();
  }

  @Override
  public TypeName trigger() {
    return ClassName.get(URI.class);
  }
}
