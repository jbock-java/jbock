package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveCharacterCoercion extends BasicCharacterCoercion {

  PrimitiveCharacterCoercion() {
    super(Character.TYPE);
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Character.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.charValue()", param).build();
  }
}
