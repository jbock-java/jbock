package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveIntCoercion extends BasicIntegerCoercion {

  PrimitiveIntCoercion() {
    super(Integer.TYPE);
  }

  @Override
  public boolean special() {
    return true;
  }

  @Override
  public TypeName paramType() {
    return TypeName.get(Integer.class);
  }
}
