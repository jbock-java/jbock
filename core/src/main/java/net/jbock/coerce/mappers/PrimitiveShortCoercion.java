package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class PrimitiveShortCoercion extends BasicShortCoercion {

  PrimitiveShortCoercion() {
    super(TypeTool.get().getPrimitiveType(TypeKind.SHORT));
  }

  @Override
  TypeMirror paramType() {
    return TypeTool.get().asType(Short.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.of("$N.shortValue()", param);
  }
}
