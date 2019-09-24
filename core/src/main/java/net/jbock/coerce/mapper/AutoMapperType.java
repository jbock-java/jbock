package net.jbock.coerce.mapper;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;
import java.util.Collections;

public class AutoMapperType extends MapperType {

  private final CodeBlock mapExpr;

  private final TypeMirror innerType; // what the function returns

  AutoMapperType(TypeMirror innerType, CodeBlock mapExpr) {
    super(false, Collections.emptyList());
    this.mapExpr = mapExpr;
    this.innerType = innerType;
  }

  @Override
  public TypeMirror innerType() {
    return innerType;
  }

  @Override
  public CodeBlock mapExpr() {
    return mapExpr;
  }
}
