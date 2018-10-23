package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicDoubleCoercion extends BasicNumberCoercion {

  BasicDoubleCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicDoubleCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Double.class).build();
  }
}
