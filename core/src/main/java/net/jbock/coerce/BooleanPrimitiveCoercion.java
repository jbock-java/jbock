package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;

class BooleanPrimitiveCoercion extends BasicBooleanCoercion {

  @Override
  TypeName trigger() {
    return TypeName.BOOLEAN;
  }
}
