package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

class ObjectBooleanCoercion extends BasicBooleanCoercion {

  @Override
  public TypeName trigger() {
    return ClassName.get(Boolean.class);
  }
}
