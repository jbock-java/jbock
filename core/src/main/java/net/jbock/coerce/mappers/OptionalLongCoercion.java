package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;

import java.util.OptionalLong;

import static net.jbock.compiler.Util.optionalOf;

class OptionalLongCoercion extends BasicLongCoercion {

  OptionalLongCoercion() {
    super(OptionalLong.class);
  }

  @Override
  TypeName paramType() {
    return optionalOf(TypeName.get(Long.class));
  }

  @Override
  public boolean handlesOptionalPrimitive() {
    return true;
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.isPresent() ? $T.of($N.get().longValue()) : $T.empty()",
        param, OptionalLong.class, param, OptionalLong.class).build();
  }
}
