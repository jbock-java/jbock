package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;

import javax.lang.model.type.TypeMirror;

public class UnwrapSuccess {

  private final TypeMirror typeArg;
  private final ParameterSpec constructorParam;
  private final CodeBlock extractExpr;
  private final int rank;

  private UnwrapSuccess(
      TypeMirror typeArg,
      ParameterSpec constructorParam,
      CodeBlock extractExpr,
      int rank) {
    this.typeArg = typeArg;
    this.constructorParam = constructorParam;
    this.extractExpr = extractExpr;
    this.rank = rank;
  }

  public static UnwrapSuccess create(TypeMirror wrappedType, ParameterSpec constructorParam, int rank) {
    return create(wrappedType, constructorParam, rank, CodeBlock.of("$N", constructorParam));
  }

  public static UnwrapSuccess create(
      TypeMirror wrappedType,
      ParameterSpec constructorParam,
      int rank,
      CodeBlock extractExpr) {
    return new UnwrapSuccess(wrappedType, constructorParam, extractExpr, rank);
  }

  public TypeMirror typeArg() {
    return typeArg;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public CodeBlock extractExpr() {
    return extractExpr;
  }

  public int rank() {
    return rank;
  }
}
