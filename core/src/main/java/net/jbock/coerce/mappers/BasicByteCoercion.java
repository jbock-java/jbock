package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicByteCoercion extends CoercionFactory {

  BasicByteCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicByteCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Byte.class).build();
  }
}
