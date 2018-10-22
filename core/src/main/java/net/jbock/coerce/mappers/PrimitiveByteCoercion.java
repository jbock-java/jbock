package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveByteCoercion extends BasicByteCoercion {

  PrimitiveByteCoercion() {
    super(TypeTool.get().primitive(TypeKind.BYTE));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().declared(Byte.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.byteValue()", param).build();
  }
}
