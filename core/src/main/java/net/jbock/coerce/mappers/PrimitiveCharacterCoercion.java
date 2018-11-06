package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveCharacterCoercion extends BasicCharacterCoercion {

  PrimitiveCharacterCoercion() {
    super(TypeTool.get().getPrimitiveType(TypeKind.CHAR));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().asType(Character.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.of("$N.charValue()", param);
  }
}
