package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveShortCoercion extends BasicShortCoercion {

  PrimitiveShortCoercion() {
    super(TypeTool.get().primitive(TypeKind.SHORT));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().declared(Short.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.shortValue()", param).build();
  }
}
