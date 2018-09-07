package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

class BooleanObjectCoercion extends BasicBooleanCoercion {

  @Override
  TypeName trigger() {
    return ClassName.get(Boolean.class);
  }
}
