package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

class IntegerCoercion extends BasicIntegerCoercion {

  @Override
  TypeName trigger() {
    return ClassName.get(Integer.class);
  }

  @Override
  boolean special() {
    return false;
  }
}
