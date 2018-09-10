package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;

import java.util.Objects;

abstract class BasicIntegerCoercion extends Coercion {

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Integer.class).build();
  }

  @Override
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder().add("$L", param).build();
  }

  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map($T::toString)", Objects.class).build();
  }
}
