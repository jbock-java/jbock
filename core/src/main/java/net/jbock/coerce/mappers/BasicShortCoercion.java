package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicShortCoercion extends CoercionFactory {

  BasicShortCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicShortCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Short.class).build();
  }
}
