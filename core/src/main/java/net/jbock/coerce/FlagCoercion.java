package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.ParamName;

import java.util.function.Function;

public class FlagCoercion extends Coercion {

  public FlagCoercion(
      ParamName paramName,
      ParameterSpec constructorParam,
      FieldSpec field) {
    super(constructorParam, field, paramName);
  }

  public Skew getSkew() {
    return Skew.FLAG;
  }

  public CodeBlock extractExpr() {
    return CodeBlock.of("$N", constructorParam());
  }

  public CodeBlock collectExpr() {
    return CodeBlock.of(".findAny().isPresent()");
  }

  public CodeBlock mapExpr() {
    return CodeBlock.of("$T.identity()", Function.class);
  }
}
