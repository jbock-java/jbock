package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveFloatCoercion extends BasicFloatCoercion {

  PrimitiveFloatCoercion() {
    super(Float.TYPE);
  }

  @Override
  boolean special() {
    return true;
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Float.class);
  }
}
