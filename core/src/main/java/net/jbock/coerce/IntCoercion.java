package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

import static net.jbock.com.squareup.javapoet.TypeName.INT;

class IntCoercion extends BasicIntegerCoercion {

  @Override
  TypeName trigger() {
    return INT;
  }

  @Override
  boolean special() {
    return true;
  }
}
