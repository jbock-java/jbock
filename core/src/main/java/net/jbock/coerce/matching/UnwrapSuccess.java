package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.type.TypeMirror;

class UnwrapSuccess {

  private final TypeMirror wrappedType;
  private final ParameterSpec constructorParam;
  private final CodeBlock extractExpr;

  UnwrapSuccess(
      TypeMirror wrappedType,
      ParameterSpec constructorParam,
      CodeBlock extractExpr) {
    this.wrappedType = wrappedType;
    this.constructorParam = constructorParam;
    this.extractExpr = extractExpr;
  }

  TypeMirror wrappedType() {
    return wrappedType;
  }

  ParameterSpec constructorParam() {
    return constructorParam;
  }

  CodeBlock extractExpr() {
    return extractExpr;
  }
}
