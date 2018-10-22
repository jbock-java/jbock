package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveFloatCoercion extends BasicFloatCoercion {

  PrimitiveFloatCoercion() {
    super(TypeTool.get().primitive(TypeKind.FLOAT));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().declared(Float.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.floatValue()", param).build();
  }
}
