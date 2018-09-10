package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveLongCoercion extends BasicLongCoercion {

  @Override
  public TypeName trigger() {
    return TypeName.LONG;
  }

  @Override
  public boolean special() {
    return true;
  }
}
