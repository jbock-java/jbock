package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveBooleanCoercion extends BasicBooleanCoercion {

  @Override
  public TypeName trigger() {
    return TypeName.BOOLEAN;
  }
}
