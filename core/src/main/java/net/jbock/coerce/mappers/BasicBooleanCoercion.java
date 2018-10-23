package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.PrimitiveType;

abstract class BasicBooleanCoercion extends CoercionFactory {

  BasicBooleanCoercion(Class<?> mapperReturnType) {
    super(mapperReturnType);
  }

  BasicBooleanCoercion(PrimitiveType mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", Boolean.class).build();
  }
}
