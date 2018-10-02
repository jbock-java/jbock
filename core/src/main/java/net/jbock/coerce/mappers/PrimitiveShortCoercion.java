package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;

class PrimitiveShortCoercion extends BasicShortCoercion {

  PrimitiveShortCoercion() {
    super(TypeTool.get().primitive(TypeKind.SHORT));
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Short.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.shortValue()", param).build();
  }
}
