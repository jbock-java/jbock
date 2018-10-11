package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import java.net.URI;

class URICoercion extends CoercionFactory {

  URICoercion() {
    super(URI.class);
  }

  @Override
  CodeBlock map() {
    return CodeBlock.builder().add(".map($T::create)", URI.class).build();
  }
}
