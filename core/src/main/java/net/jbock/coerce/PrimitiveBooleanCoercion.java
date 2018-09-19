package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveBooleanCoercion extends BasicBooleanCoercion {

  PrimitiveBooleanCoercion() {
    super(Boolean.TYPE);
  }

  @Override
  public TypeName paramType() {
    return TypeName.get(Boolean.class);
  }
}
