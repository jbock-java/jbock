package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveFloatCoercion extends BasicFloatCoercion {

  PrimitiveFloatCoercion() {
    super(Float.TYPE);
  }

  @Override
  public boolean special() {
    return true;
  }

  @Override
  public TypeName paramType() {
    return TypeName.get(Float.class);
  }
}
