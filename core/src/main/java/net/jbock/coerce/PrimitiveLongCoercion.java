package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveLongCoercion extends BasicLongCoercion {

  PrimitiveLongCoercion() {
    super(Long.TYPE);
  }

  @Override
  boolean special() {
    return true;
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Long.class);
  }
}
