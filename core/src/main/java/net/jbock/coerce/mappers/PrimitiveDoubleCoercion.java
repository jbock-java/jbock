package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveDoubleCoercion extends BasicDoubleCoercion {

  PrimitiveDoubleCoercion() {
    super(Double.TYPE);
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Double.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.doubleValue()", param).build();
  }
}
