package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveDoubleCoercion extends BasicDoubleCoercion {

  PrimitiveDoubleCoercion() {
    super(Double.TYPE);
  }

  @Override
  public boolean special() {
    return true;
  }

  @Override
  public TypeName paramType() {
    return TypeName.get(Double.class);
  }
}
