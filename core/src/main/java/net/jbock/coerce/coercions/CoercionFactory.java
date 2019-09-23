package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

public abstract class CoercionFactory {

  /**
   * Creates a function that maps from String to innerType
   */
  public abstract CodeBlock createMapper(TypeMirror innerType);
}
