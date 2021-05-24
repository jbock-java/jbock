package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Skew;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public class Match {

  private final TypeMirror baseType;
  private final ParameterSpec constructorParam;
  private final Optional<CodeBlock> extractExpr;
  private final Skew skew;
  private final EnumName enumName;

  Match(
      TypeMirror baseType,
      ParameterSpec constructorParam,
      Skew skew,
      EnumName enumName,
      Optional<CodeBlock> extractExpr) {
    this.baseType = baseType;
    this.constructorParam = constructorParam;
    this.skew = skew;
    this.extractExpr = extractExpr;
    this.enumName = enumName;
  }

  public <P extends AbstractParameter> ConvertedParameter<P> toConvertedParameter(
      CodeBlock mapExpr, P parameter) {
    return ConvertedParameter.create(mapExpr,
        extractExpr, skew, enumName, parameter);
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
