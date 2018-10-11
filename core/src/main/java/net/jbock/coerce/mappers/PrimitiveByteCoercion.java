package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;

class PrimitiveByteCoercion extends BasicByteCoercion {

  PrimitiveByteCoercion() {
    super(TypeTool.get().primitive(TypeKind.BYTE));
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Byte.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.byteValue()", param).build();
  }
}
