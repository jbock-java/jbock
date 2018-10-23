package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

public final class EnumCoercion extends CoercionFactory {

  private EnumCoercion(TypeMirror mapperReturnType) {
    super(mapperReturnType);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", mapperReturnType()).build();
  }

  public static EnumCoercion create(TypeMirror mapperReturnType) {
    return new EnumCoercion(mapperReturnType);
  }
}
