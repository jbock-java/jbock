package net.jbock.coerce.mappers;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

public final class EnumCoercion extends CoercionFactory {

  private EnumCoercion(TypeMirror trigger) {
    super(trigger);
  }

  @Override
  final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", trigger()).build();
  }

  public static EnumCoercion create(TypeMirror trigger) {
    return new EnumCoercion(trigger);
  }
}
