package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.parameter.AbstractParameter;

public final class ConvertedParameter<P extends AbstractParameter> {

  private final ParameterSpec constructorParam;
  private final CodeBlock mapExpr;
  private final CodeBlock extractExpr;
  private final Skew skew;
  private final P parameter;
  private final EnumName enumName;

  public ConvertedParameter(
      CodeBlock mapExpr,
      CodeBlock extractExpr,
      Skew skew,
      ParameterSpec constructorParam,
      EnumName enumName,
      P parameter) {
    this.constructorParam = constructorParam;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew;
    this.parameter = parameter;
    this.enumName = enumName;
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public CodeBlock extractExpr() {
    return extractExpr;
  }

  public Skew skew() {
    return skew;
  }

  public ParameterSpec constructorParam() {
    return constructorParam;
  }

  public EnumName enumName() {
    return enumName;
  }

  public boolean isRequired() {
    return skew == Skew.REQUIRED;
  }

  public boolean isRepeatable() {
    return skew == Skew.REPEATABLE;
  }

  public boolean isOptional() {
    return skew == Skew.OPTIONAL;
  }

  public boolean isFlag() {
    return skew == Skew.FLAG;
  }

  public P parameter() {
    return parameter;
  }

  public String paramLabel() {
    return parameter.paramLabel();
  }

  public String enumConstant() {
    return enumName.enumConstant();
  }
}
