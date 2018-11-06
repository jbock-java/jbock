package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveLongCoercion extends BasicLongCoercion {

  PrimitiveLongCoercion() {
    super(TypeTool.get().getPrimitiveType(TypeKind.LONG));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().asType(Long.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.longValue()", param).build();
  }
}
