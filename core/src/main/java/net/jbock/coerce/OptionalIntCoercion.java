package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.OptionalInt;

import static net.jbock.compiler.Util.optionalOf;

class OptionalIntCoercion extends BasicIntegerCoercion {

  OptionalIntCoercion() {
    super(OptionalInt.class);
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
        .add("($L.isPresent() ? $L.getAsInt() : $S)",
            param,
            param,
            "null")
        .build();
  }

  @Override
  TypeName paramType() {
    return optionalOf(TypeName.get(Integer.class));
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.isPresent() ? $T.of($N.get()) : $T.empty()",
        param, OptionalInt.class, param, OptionalInt.class).build();
  }
}
