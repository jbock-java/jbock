package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveDoubleCoercion extends BasicDoubleCoercion {

  @Override
  public TypeName trigger() {
    return TypeName.DOUBLE;
  }

  @Override
  public boolean special() {
    return true;
  }
}
