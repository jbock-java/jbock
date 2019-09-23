package net.jbock.coerce.coercions;

import com.squareup.javapoet.CodeBlock;

class SimpleCoercion extends CoercionFactory {

  private final CodeBlock mapExpr;

  private SimpleCoercion(CodeBlock mapExpr) {
    this.mapExpr = mapExpr;
  }

  static SimpleCoercion create(Class<?> clasz, String createFromString) {
    return new SimpleCoercion(CodeBlock.of("$T::" + createFromString, clasz));
  }

  static SimpleCoercion create(CodeBlock mapExpr) {
    return new SimpleCoercion(mapExpr);
  }

  @Override
  public final CodeBlock mapExpr() {
    return mapExpr;
  }
}
