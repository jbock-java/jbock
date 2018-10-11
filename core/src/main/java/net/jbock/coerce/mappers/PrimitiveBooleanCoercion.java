package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;

class PrimitiveBooleanCoercion extends BasicBooleanCoercion {

  PrimitiveBooleanCoercion() {
    super(TypeTool.get().primitive(TypeKind.BOOLEAN));
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Boolean.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.booleanValue()", param).build();
  }
}
