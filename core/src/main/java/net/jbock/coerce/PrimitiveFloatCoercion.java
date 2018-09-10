package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveFloatCoercion extends BasicFloatCoercion {

  @Override
  public TypeName trigger() {
    return TypeName.FLOAT;
  }

  @Override
  public boolean special() {
    return true;
  }
}
