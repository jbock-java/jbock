package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

class ObjectLongCoercion extends BasicLongCoercion {

  @Override
  public TypeName trigger() {
    return ClassName.get(Long.class);
  }

  @Override
  public boolean special() {
    return false;
  }
}
