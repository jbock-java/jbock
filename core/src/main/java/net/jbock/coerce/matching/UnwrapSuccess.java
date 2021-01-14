package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

class UnwrapSuccess {

  private final TypeMirror wrappedType;
  private final TypeMirror constructorParamType;
  private final Function<ParameterSpec, CodeBlock> extractExpr;

  UnwrapSuccess(
      TypeMirror wrappedType,
      TypeMirror constructorParamType,
      Function<ParameterSpec, CodeBlock> extractExpr) {
    this.wrappedType = wrappedType;
    this.constructorParamType = constructorParamType;
    this.extractExpr = extractExpr;
  }

  TypeMirror wrappedType() {
    return wrappedType;
  }

  TypeMirror constructorParamType() {
    return constructorParamType;
  }

  CodeBlock extractExpr(ParameterSpec constructorParam) {
    return extractExpr.apply(constructorParam);
  }
}
