package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Skew;

import javax.lang.model.type.TypeMirror;

public class Match {

  private final TypeMirror typeArg;
  private final ParameterSpec constructorParam;
  private final CodeBlock extractExpr;
  private final Skew skew;

  private Match(
      TypeMirror typeArg,
      ParameterSpec constructorParam,
      CodeBlock extractExpr,
      Skew skew) {
    this.typeArg = typeArg;
    this.constructorParam = constructorParam;
    this.extractExpr = extractExpr;
    this.skew = skew;
  }

  public static Match create(TypeMirror wrappedType, ParameterSpec constructorParam, Skew skew) {
    return create(wrappedType, constructorParam, skew, CodeBlock.of("$N", constructorParam));
  }

  public static Match create(
      TypeMirror wrappedType,
      ParameterSpec constructorParam,
      Skew skew,
      CodeBlock extractExpr) {
    return new Match(wrappedType, constructorParam, extractExpr, skew);
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

  public Skew skew() {
    return skew;
  }
}
