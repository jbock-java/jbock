package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

class ObjectDoubleCoercion extends BasicDoubleCoercion {

  @Override
  public TypeName trigger() {
    return ClassName.get(Double.class);
  }
}
