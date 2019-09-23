package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

public final class EnumCoercion extends CoercionFactory {

  private final CodeBlock mapExpr;

  private EnumCoercion(CodeBlock mapExpr) {
    this.mapExpr = mapExpr;
  }

  @Override
  public final CodeBlock mapExpr() {
    return mapExpr;
  }

  public static EnumCoercion create(TypeMirror enumType) {
    return new EnumCoercion(CodeBlock.of("$T::valueOf", enumType));
  }
}
