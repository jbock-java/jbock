package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.qualifier.ConstructorParam;
import net.jbock.compiler.EnumName;
import net.jbock.qualifier.ExtractExpr;
import net.jbock.qualifier.MapExpr;
import net.jbock.qualifier.TailExpr;

import javax.inject.Inject;

public class Coercion {

  private final ParameterSpec constructorParam;
  private final EnumName enumName;
  private final CodeBlock tailExpr;
  private final CodeBlock mapExpr;
  private final CodeBlock extractExpr;
  private final Skew skew;

  @Inject
  Coercion(
      EnumName enumName,
      @MapExpr CodeBlock mapExpr,
      @TailExpr CodeBlock tailExpr,
      @ExtractExpr CodeBlock extractExpr,
      Skew skew,
      @ConstructorParam ParameterSpec constructorParam) {
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
