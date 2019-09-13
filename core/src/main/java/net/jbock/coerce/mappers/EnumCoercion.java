package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

public final class EnumCoercion extends CoercionFactory {

  @Override
  final CodeBlock createMapper(TypeMirror innerType) {
    return CodeBlock.of("$T::valueOf", innerType);
  }

  public static EnumCoercion create() {
    return new EnumCoercion();
  }
}
