package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.OptionalInt;

class OptionalIntCoercion extends BasicIntegerCoercion {

  OptionalIntCoercion() {
    super(OptionalInt.class);
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().optionalOf(Integer.class);
  }

  @Override
  public boolean handlesOptionalPrimitive() {
    return true;
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.of("$N.isPresent() ? $T.of($N.get().intValue()) : $T.empty()",
        param, OptionalInt.class, param, OptionalInt.class);
  }
}
