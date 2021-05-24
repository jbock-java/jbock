package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Skew;

import javax.lang.model.type.TypeMirror;

public class Match {

  private final TypeMirror baseType;
  private final ParameterSpec constructorParam;
  private final CodeBlock extractExpr;
  private final Skew skew;
  private final EnumName enumName;

  Match(
      TypeMirror baseType,
      ParameterSpec constructorParam,
      CodeBlock extractExpr,
      Skew skew,
      EnumName enumName) {
    this.baseType = baseType;
    this.constructorParam = constructorParam;
    this.extractExpr = extractExpr;
    this.skew = skew;
    this.enumName = enumName;
  }

  public <P extends AbstractParameter> ConvertedParameter<P> toConvertedParameter(
      CodeBlock mapExpr, P parameter) {
    return ConvertedParameter.create(mapExpr,
        extractExpr, skew, constructorParam, enumName, parameter);
  }

  public TypeMirror baseType() {
    return baseType;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public Skew skew() {
    return skew;
  }
}
