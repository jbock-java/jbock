package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveIntCoercion extends BasicIntegerCoercion {

  PrimitiveIntCoercion() {
    super(TypeTool.get().primitive(TypeKind.INT));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().declared(Integer.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.intValue()", param).build();
  }
}
