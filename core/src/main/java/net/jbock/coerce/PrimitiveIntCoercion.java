package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveIntCoercion extends BasicIntegerCoercion {

  PrimitiveIntCoercion() {
    super(Integer.TYPE);
  }

  @Override
  boolean special() {
    return true;
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Integer.class);
  }
}
