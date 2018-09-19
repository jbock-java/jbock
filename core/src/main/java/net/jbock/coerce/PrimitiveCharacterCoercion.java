package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class PrimitiveCharacterCoercion extends BasicCharacterCoercion {

  PrimitiveCharacterCoercion() {
    super(Character.TYPE);
  }

  @Override
  boolean special() {
    return true;
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Character.class);
  }
}
