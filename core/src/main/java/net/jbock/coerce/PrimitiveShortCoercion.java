package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveShortCoercion extends BasicShortCoercion {

  PrimitiveShortCoercion() {
    super(Short.TYPE);
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Short.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.shortValue()", param).build();
  }
}
