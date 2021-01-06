package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.ParamName;

import javax.lang.model.element.TypeElement;
import java.util.List;

public abstract class Coercion {

  private final ParameterSpec constructorParam;

  private final ParamName paramName;

  private final List<TypeElement> originatingElements;

  Coercion(ParameterSpec constructorParam, ParamName paramName, List<TypeElement> originatingElements) {
    this.constructorParam = constructorParam;
    this.paramName = paramName;
    this.originatingElements = originatingElements;
  }

  public final ParameterSpec constructorParam() {
    return constructorParam;
  }

  public final ParamName paramName() {
    return paramName;
  }

  public abstract CodeBlock mapExpr();

  public abstract CodeBlock extractExpr();

  public abstract CodeBlock collectExpr();

  public abstract Skew getSkew();

  public List<TypeElement> originatingElements() {
    return originatingElements;
  }
}
