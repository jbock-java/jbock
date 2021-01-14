package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

class UnwrapSuccess {

  private final TypeMirror wrappedType;
  private final TypeMirror liftedType;
  private final Function<ParameterSpec, CodeBlock> extractExpr;

  UnwrapSuccess(TypeMirror wrappedType, TypeMirror liftedType, Function<ParameterSpec, CodeBlock> extractExpr) {
    this.wrappedType = wrappedType;
    this.liftedType = liftedType;
    this.extractExpr = extractExpr;
  }

  TypeMirror wrappedType() {
    return wrappedType;
  }

  TypeMirror liftedType() {
    return liftedType;
  }

  CodeBlock extractExpr(ParameterSpec constructorParam) {
    return extractExpr.apply(constructorParam);
  }
}
