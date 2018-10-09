package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.OptionalDouble;

import static net.jbock.compiler.Util.optionalOf;

class OptionalDoubleCoercion extends BasicDoubleCoercion {

  OptionalDoubleCoercion() {
    super(OptionalDouble.class);
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
