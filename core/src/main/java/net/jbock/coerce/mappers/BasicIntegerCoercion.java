package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicIntegerCoercion extends BasicNumberCoercion {

  BasicIntegerCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicIntegerCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Integer.class).build();
  }

}
