package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveBooleanCoercion extends BasicBooleanCoercion {

  PrimitiveBooleanCoercion() {
    super(TypeTool.get().getPrimitiveType(TypeKind.BOOLEAN));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().asType(Boolean.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.of("$N.booleanValue()", param);
  }
}
