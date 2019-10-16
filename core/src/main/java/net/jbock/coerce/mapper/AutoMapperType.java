package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;

import java.util.Collections;

public class AutoMapperType extends MapperType {

  private final CodeBlock mapExpr;

  AutoMapperType(CodeBlock mapExpr) {
    super(false, Collections.emptyList());
    this.mapExpr = mapExpr;
  }

  @Override
  public CodeBlock mapExpr() {
    return mapExpr;
  }
}
