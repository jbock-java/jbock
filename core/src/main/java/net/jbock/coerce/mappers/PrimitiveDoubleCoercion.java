package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.type.TypeKind;

class PrimitiveDoubleCoercion extends BasicDoubleCoercion {

  PrimitiveDoubleCoercion() {
    super(TypeTool.get().primitive(TypeKind.DOUBLE));
  }

  @Override
  TypeName paramType() {
    return TypeName.get(Double.class);
  }

  @Override
  CodeBlock extract(ParameterSpec param) {
    return CodeBlock.builder().add("$N.doubleValue()", param).build();
  }
}
