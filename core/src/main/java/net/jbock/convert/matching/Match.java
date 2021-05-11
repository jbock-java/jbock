package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Skew;

import javax.lang.model.type.TypeMirror;

public class Match {

  private final TypeMirror baseReturnType;
  private final ParameterSpec constructorParam;
  private final CodeBlock extractExpr;
  private final Skew skew;
  private final EnumName enumName;

  Match(
      TypeMirror baseReturnType,
      ParameterSpec constructorParam,
      CodeBlock extractExpr,
      Skew skew,
      EnumName enumName) {
    this.baseReturnType = baseReturnType;
    this.constructorParam = constructorParam;
    this.extractExpr = extractExpr;
    this.skew = skew;
    this.enumName = enumName;
  }

  public <P extends AbstractParameter> ConvertedParameter<P> toCoercion(
      CodeBlock mapExpr, P parameter) {
    return new ConvertedParameter<>(mapExpr,
        extractExpr, skew, constructorParam, enumName, parameter);
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
