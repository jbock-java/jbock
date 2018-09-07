package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;

abstract class BasicBooleanCoercion extends Coercion {

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().build();
  }

  @Override
  public final boolean special() {
    return true;
  }

  @Override
  public CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("$L", param).build();
  }
}
