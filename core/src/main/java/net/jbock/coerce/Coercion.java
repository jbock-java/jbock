package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;

public class Coercion {

  private final ParameterSpec constructorParam;
  private final EnumName enumName;
  private final CodeBlock tailExpr;
  private final CodeBlock mapExpr;
  private final CodeBlock extractExpr;
  private final Skew skew;

  public Coercion(
      EnumName enumName,
      CodeBlock mapExpr,
      CodeBlock tailExpr,
      CodeBlock extractExpr,
      Skew skew,
      ParameterSpec constructorParam) {
    this.constructorParam = constructorParam;
    this.enumName = enumName;
    this.tailExpr = tailExpr;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew;
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public CodeBlock extractExpr() {
    return extractExpr;
  }

  public CodeBlock tailExpr() {
    return tailExpr;
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
}
