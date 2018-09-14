package net.jbock.coerce;

import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.TypeName;

final class EnumCoercion extends Coercion {

  private final TypeName trigger;

  private EnumCoercion(TypeName trigger) {
    this.trigger = trigger;
  }

  @Override
  public final CodeBlock map() {
    return CodeBlock.builder().add(".map($T::valueOf)", trigger()).build();
  }

  static EnumCoercion create(TypeName trigger) {
    return new EnumCoercion(trigger);
  }

  @Override
  public TypeName trigger() {
    return trigger;
  }
}
