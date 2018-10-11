package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;

class PrimitiveCharacterCoercion extends BasicCharacterCoercion {

  PrimitiveCharacterCoercion() {
    super(TypeTool.get().primitive(TypeKind.CHAR));
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Character.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.charValue()", param).build();
  }
}
