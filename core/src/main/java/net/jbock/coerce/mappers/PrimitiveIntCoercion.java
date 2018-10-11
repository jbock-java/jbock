package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;

class PrimitiveIntCoercion extends BasicIntegerCoercion {

  PrimitiveIntCoercion() {
    super(TypeTool.get().primitive(TypeKind.INT));
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Integer.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.intValue()", param).build();
  }
}
