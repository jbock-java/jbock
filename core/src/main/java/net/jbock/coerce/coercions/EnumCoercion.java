package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

public final class EnumCoercion extends CoercionFactory {

  @Override
  public final CodeBlock createMapper(TypeMirror innerType) {
    return CodeBlock.of("$T::valueOf", innerType);
  }

  public static EnumCoercion create() {
    return new EnumCoercion();
  }
}
