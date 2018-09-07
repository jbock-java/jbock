package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.Constants;

class StringArrayCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().build();
  }

  @Override
  public TypeName trigger() {
    return Constants.STRING_ARRAY;
  }

  @Override
  boolean special() {
    return true;
  }
}
