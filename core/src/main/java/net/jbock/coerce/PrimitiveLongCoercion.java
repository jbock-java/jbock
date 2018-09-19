package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveLongCoercion extends BasicLongCoercion {

  PrimitiveLongCoercion() {
    super(Long.TYPE);
  }

  @Override
  public boolean special() {
    return true;
  }

  @Override
  public TypeName paramType() {
    return TypeName.get(Long.class);
  }
}
