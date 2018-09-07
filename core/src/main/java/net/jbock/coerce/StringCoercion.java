package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

import static net.jbock.compiler.Constants.STRING;

class StringCoercion extends Coercion {

  @Override
  public CodeBlock map() {
    return CodeBlock.builder().build();
  }

  @Override
  public TypeName trigger() {
    return STRING;
  }

  @Override
  boolean special() {
    return false;
  }
}
