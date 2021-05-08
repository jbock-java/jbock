package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Skew;
import net.jbock.compiler.parameter.AbstractParameter;

import javax.lang.model.type.TypeMirror;

public class Match {

  private final TypeMirror baseReturnType;
  private final ParameterSpec constructorParam;
  private final CodeBlock extractExpr;
  private final CodeBlock tailExpr;
  private final Skew skew;

  private Match(
      TypeMirror baseReturnType,
      ParameterSpec constructorParam,
      CodeBlock extractExpr,
      CodeBlock tailExpr,
      Skew skew) {
    this.baseReturnType = baseReturnType;
    this.constructorParam = constructorParam;
    this.extractExpr = extractExpr;
    this.tailExpr = tailExpr;
    this.skew = skew;
  }

  public static Match create(TypeMirror wrappedType, ParameterSpec constructorParam, Skew skew, CodeBlock tailExpr) {
    return create(wrappedType, constructorParam, skew, tailExpr, CodeBlock.of("$N", constructorParam));
  }

  public static Match create(
      TypeMirror wrappedType,
      ParameterSpec constructorParam,
      Skew skew,
      CodeBlock tailExpr,
      CodeBlock extractExpr) {
    return new Match(wrappedType, constructorParam, extractExpr, tailExpr, skew);
  }

  public <P extends AbstractParameter> ConvertedParameter<P> toCoercion(CodeBlock mapExpr, P parameter) {
    return new ConvertedParameter<>(mapExpr, tailExpr,
        extractExpr, skew, constructorParam, parameter);
  }

  public TypeMirror baseReturnType() {
    return baseReturnType;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public Skew skew() {
    return skew;
  }
}
