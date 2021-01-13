package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.EnumName;

public abstract class Coercion {

  private final ParameterSpec constructorParam;

  private final EnumName paramName;

  Coercion(ParameterSpec constructorParam, EnumName paramName) {
    this.constructorParam = constructorParam;
    this.paramName = paramName;
  }

  public final ParameterSpec constructorParam() {
    return constructorParam;
  }

  public final EnumName paramName() {
    return paramName;
  }

  public abstract CodeBlock mapExpr();

  public abstract CodeBlock extractExpr();

  public abstract CodeBlock collectExpr();

  public abstract Skew getSkew();
}
