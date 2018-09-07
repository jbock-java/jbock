package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

public abstract class Coercion {

  public abstract CodeBlock map();

  abstract TypeName trigger();

  /**
   * Specials can't be in Optional or List
   */
  abstract boolean special();

}
