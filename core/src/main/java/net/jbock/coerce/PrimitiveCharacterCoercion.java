package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveCharacterCoercion extends BasicCharacterCoercion {

  @Override
  public TypeName trigger() {
    return TypeName.CHAR;
  }

  @Override
  public boolean special() {
    return true;
  }
}
