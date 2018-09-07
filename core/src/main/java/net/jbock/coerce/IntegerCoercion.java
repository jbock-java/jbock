package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

class IntegerCoercion extends BasicIntegerCoercion {

  @Override
  public TypeName trigger() {
    return ClassName.get(Integer.class);
  }

  @Override
  public boolean special() {
    return false;
  }
}
