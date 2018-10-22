package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveBooleanCoercion extends BasicBooleanCoercion {

  PrimitiveBooleanCoercion() {
    super(TypeTool.get().primitive(TypeKind.BOOLEAN));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().declared(Boolean.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.booleanValue()", param).build();
  }
}
