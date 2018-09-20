package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveFloatCoercion extends BasicFloatCoercion {

  PrimitiveFloatCoercion() {
    super(Float.TYPE);
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Float.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.floatValue()", param).build();
  }
}
