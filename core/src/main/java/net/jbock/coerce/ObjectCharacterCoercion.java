package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.TypeName;

class ObjectCharacterCoercion extends BasicCharacterCoercion {

  @Override
  public TypeName trigger() {
    return ClassName.get(Character.class);
  }
}
