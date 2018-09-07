package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

import static net.jbock.com.squareup.javapoet.TypeName.INT;

class IntCoercion extends BasicIntegerCoercion {

  @Override
  public TypeName trigger() {
    return INT;
  }

  @Override
  public boolean special() {
    return true;
  }
}
