package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;

public class NonFlagCoercion extends Coercion {

  private final CodeBlock collectExpr;

  private final CodeBlock mapExpr;

  private final CodeBlock extractExpr;

  private final Skew skew;

  public NonFlagCoercion(EnumName enumName, CodeBlock mapExpr, CodeBlock collectExpr, CodeBlock extractExpr,
                         NonFlagSkew skew, ParameterSpec constructorParam) {
    super(constructorParam, enumName);
    this.collectExpr = collectExpr;
    this.mapExpr = mapExpr;
    this.extractExpr = extractExpr;
    this.skew = skew.widen();
  }

  public CodeBlock mapExpr() {
    return mapExpr;
  }

  public CodeBlock extractExpr() {
    return extractExpr;
  }

  public CodeBlock collectExpr() {
    return collectExpr;
  }

  public Skew getSkew() {
    return skew;
  }
}
