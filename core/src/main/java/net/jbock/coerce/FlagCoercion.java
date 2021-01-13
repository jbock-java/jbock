package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;

import javax.lang.model.element.ExecutableElement;
import java.util.function.Function;

public class FlagCoercion extends Coercion {

  public FlagCoercion(EnumName enumName, ExecutableElement sourceMethod) {
    super(ParameterSpec.builder(TypeName.get(sourceMethod.getReturnType()), enumName.snake()).build(), enumName);
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
