package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveCharacterCoercion extends BasicCharacterCoercion {

  PrimitiveCharacterCoercion() {
    super(Character.TYPE);
  }

  @Override
  public boolean special() {
    return true;
  }

  @Override
  public TypeName paramType() {
    return TypeName.get(Character.class);
  }
}
