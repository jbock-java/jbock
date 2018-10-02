package net.jbock.coerce.mappers;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;

class PrimitiveLongCoercion extends BasicLongCoercion {

  PrimitiveLongCoercion() {
    super(TypeTool.get().primitive(TypeKind.LONG));
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Long.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.longValue()", param).build();
  }
}
