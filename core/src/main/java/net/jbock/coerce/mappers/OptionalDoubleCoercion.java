package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.OptionalDouble;

import static net.jbock.compiler.Util.optionalOf;

class OptionalDoubleCoercion extends BasicDoubleCoercion {

  OptionalDoubleCoercion() {
    super(OptionalDouble.class);
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
    return optionalOf(TypeName.get(Double.class));
  }

  @Override
  public boolean handlesOptionalPrimitive() {
    return true;
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.isPresent() ? $T.of($N.get().doubleValue()) : $T.empty()",
        param, OptionalDouble.class, param, OptionalDouble.class).build();
  }
}
