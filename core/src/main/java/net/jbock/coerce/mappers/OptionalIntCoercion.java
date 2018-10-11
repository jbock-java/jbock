package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.OptionalInt;

import static net.jbock.compiler.Util.optionalOf;

class OptionalIntCoercion extends BasicIntegerCoercion {

  OptionalIntCoercion() {
    super(OptionalInt.class);
  }

  @Override
  TypeName paramType() {
    return optionalOf(TypeName.get(Integer.class));
  }

  @Override
  public boolean handlesOptionalPrimitive() {
    return true;
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.isPresent() ? $T.of($N.get().intValue()) : $T.empty()",
        param, OptionalInt.class, param, OptionalInt.class).build();
  }
}
