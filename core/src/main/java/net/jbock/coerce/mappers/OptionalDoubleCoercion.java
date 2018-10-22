package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.OptionalDouble;

class OptionalDoubleCoercion extends BasicDoubleCoercion {

  OptionalDoubleCoercion() {
    super(OptionalDouble.class);
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().optionalOf(Double.class);
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
