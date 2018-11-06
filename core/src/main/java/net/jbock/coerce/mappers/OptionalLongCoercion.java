package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeMirror;
import java.util.OptionalLong;

class OptionalLongCoercion extends BasicLongCoercion {

  OptionalLongCoercion() {
    super(OptionalLong.class);
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().optionalOf(Long.class);
  }

  @Override
  public boolean handlesOptionalPrimitive() {
    return true;
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.of("$N.isPresent() ? $T.of($N.get().longValue()) : $T.empty()",
        param, OptionalLong.class, param, OptionalLong.class);
  }
}
