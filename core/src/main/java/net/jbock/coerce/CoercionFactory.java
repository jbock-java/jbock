package net.jbock.coerce;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.coerce.matching.MatchWithMap;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;
import java.util.function.Function;

public class CoercionFactory extends ParameterScoped {

  @Inject
  CoercionFactory(ParameterContext parameterContext) {
    super(parameterContext);
  }

  public Coercion create(MatchWithMap success) {
    CodeBlock mapExpr = success.mapExpr();
    CodeBlock tailExpr = success.tailExpr();
    CodeBlock extractExpr = success.extractExpr();
    Skew skew = success.skew();
    ParameterSpec constructorParam = success.constructorParam();
    return new Coercion(enumName(), mapExpr, tailExpr, extractExpr, skew, constructorParam);
  }

  public Coercion createFlag() {
    ParameterSpec constructorParam = ParameterSpec.builder(TypeName.get(returnType()), enumName().snake()).build();
    CodeBlock mapExpr = CodeBlock.of("$T.identity()", Function.class);
    CodeBlock tailExpr = CodeBlock.of(".findAny().isPresent()");
    CodeBlock extractExpr = CodeBlock.of("$N", constructorParam);
    return new Coercion(enumName(), mapExpr, tailExpr, extractExpr, Skew.FLAG, constructorParam);
  }
}
