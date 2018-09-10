package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

class ObjectFloatCoercion extends BasicFloatCoercion {

  @Override
  public TypeName trigger() {
    return ClassName.get(Float.class);
  }

  @Override
  public boolean special() {
    return false;
  }
}
