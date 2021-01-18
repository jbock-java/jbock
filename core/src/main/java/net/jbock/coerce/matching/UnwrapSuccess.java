package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.type.TypeMirror;

public class UnwrapSuccess {

  private final TypeMirror wrappedType;
  private final ParameterSpec constructorParam;
  private final CodeBlock extractExpr;

  private UnwrapSuccess(
      TypeMirror wrappedType,
      ParameterSpec constructorParam,
      CodeBlock extractExpr) {
    this.wrappedType = wrappedType;
    this.constructorParam = constructorParam;
    this.extractExpr = extractExpr;
  }

  public static UnwrapSuccess create(TypeMirror wrappedType, ParameterSpec constructorParam) {
    return create(wrappedType, constructorParam, CodeBlock.of("$N", constructorParam));
  }

  public static UnwrapSuccess create(
      TypeMirror wrappedType,
      ParameterSpec constructorParam,
      CodeBlock extractExpr) {
    return new UnwrapSuccess(wrappedType, constructorParam, extractExpr);
  }

  public TypeMirror wrappedType() {
    return wrappedType;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public CodeBlock extractExpr() {
    return extractExpr;
  }
}
