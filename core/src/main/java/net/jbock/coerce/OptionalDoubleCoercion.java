package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.OptionalDouble;

class OptionalDoubleCoercion extends BasicDoubleCoercion {

  OptionalDoubleCoercion() {
    super(OptionalDouble.class);
  }

  @Override
  boolean special() {
    return true;
  }

  @Override
  CodeBlock mapJsonExpr(FieldSpec field) {
    return CodeBlock.builder().add(".map($L -> $L)",
        "e", jsonExpr("e")).build();
  }

  @Override
  CodeBlock jsonExpr(String param) {
    return CodeBlock.builder()
        .add("($L.isPresent() ? $L.getAsDouble() : $S)",
            param,
            param,
            "null")
        .build();
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Double.class);
  }
}
