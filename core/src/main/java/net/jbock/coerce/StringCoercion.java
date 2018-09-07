package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
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
  public boolean special() {
    return false;
  }

  @Override
  public CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("quote.apply($L)", param).build();
  }

  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add("quote").build();
  }
}
