package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.parameter.Parameter;

import java.util.List;

public class Coercion<P extends Parameter> {

  private final ParameterSpec constructorParam;
  private final EnumName enumName;
  private final CodeBlock tailExpr;
  private final CodeBlock mapExpr;
  private final CodeBlock extractExpr;
  private final Skew skew;
  private final P parameter;

  public Coercion(
      EnumName enumName,
      CodeBlock mapExpr,
      CodeBlock tailExpr,
      CodeBlock extractExpr,
      Skew skew,
      ParameterSpec constructorParam,
      P parameter) {
    this.constructorParam = constructorParam;
    this.enumName = enumName;
    this.tailExpr = tailExpr;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew;
    this.parameter = parameter;
  }

  public final CodeBlock mapExpr() {
    return mapExpr;
  }

  public final CodeBlock extractExpr() {
    return extractExpr;
  }

  public final CodeBlock tailExpr() {
    return tailExpr;
  }

  public final Skew skew() {
    return skew;
  }

  public final ParameterSpec constructorParam() {
    return constructorParam;
  }

  public final EnumName enumName() {
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

  public final P parameter() {
    return parameter;
  }

  public String enumConstant() {
    return enumName().enumConstant();
  }

  public String sample() {
    return parameter.sample(isFlag(), enumName());
  }

  public List<String> dashedNames() {
    return parameter.dashedNames();
  }
}
