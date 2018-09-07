package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Constants;

class OptionalIntCoercion extends BasicIntegerCoercion {

  @Override
  TypeName trigger() {
    return Constants.OPTIONAL_INT;
  }

  @Override
  boolean special() {
    return true;
  }
}
