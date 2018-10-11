package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;

class PrimitiveFloatCoercion extends BasicFloatCoercion {

  PrimitiveFloatCoercion() {
    super(TypeTool.get().primitive(TypeKind.FLOAT));
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Float.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.floatValue()", param).build();
  }
}
