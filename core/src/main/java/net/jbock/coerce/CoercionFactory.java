package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.matching.Match;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;

public class CoercionFactory extends ParameterScoped {

  @Inject
  CoercionFactory(ParameterContext parameterContext) {
    super(parameterContext);
  }

  public Coercion create(CodeBlock mapExpr, Match match) {
    CodeBlock tailExpr = match.tailExpr();
    CodeBlock extractExpr = match.extractExpr();
    Skew skew = match.skew();
    ParameterSpec constructorParam = match.constructorParam();
    return new Coercion(enumName(), mapExpr, tailExpr, extractExpr, skew, constructorParam);
  }
}
