package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;

public class MapperType {

  private final CodeBlock mapExpr;

  public MapperType(CodeBlock mapExpr) {
    this.mapExpr = mapExpr;
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }
}
