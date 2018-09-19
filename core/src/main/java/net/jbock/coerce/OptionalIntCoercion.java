package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.OptionalInt;

class OptionalIntCoercion extends BasicIntegerCoercion {

  OptionalIntCoercion() {
    super(OptionalInt.class);
  }

  @Override
  public boolean special() {
    return true;
  }

  @Override
  public CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map($L -> $L)",
        "e", jsonExpr("e")).build();
  }

  @Override
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder()
        .add("($L.isPresent() ? $L.getAsInt() : $S)",
            param,
            param,
            "null")
        .build();
  }

  @Override
  public TypeName paramType() {
    return TypeName.get(Integer.class);
  }
}
