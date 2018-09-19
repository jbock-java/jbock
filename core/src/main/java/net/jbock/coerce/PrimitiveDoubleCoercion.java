package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveDoubleCoercion extends BasicDoubleCoercion {

  PrimitiveDoubleCoercion() {
    super(Double.TYPE);
  }

  @Override
  boolean special() {
    return true;
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Double.class);
  }
}
