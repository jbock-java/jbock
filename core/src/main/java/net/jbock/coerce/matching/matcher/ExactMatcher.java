package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.Skew;
import net.jbock.coerce.matching.Match;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.parameter.Parameter;

import javax.inject.Inject;
import java.util.Optional;

public class ExactMatcher extends Matcher {

  @Inject
  ExactMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  public Optional<Match> tryMatch(Parameter parameter) {
    ParameterSpec constructorParam = constructorParam(boxedReturnType());
    return Optional.of(Match.create(boxedReturnType(), constructorParam, Skew.REQUIRED, tailExpr(parameter)));
  }

  private CodeBlock tailExpr(Parameter parameter) {
    return CodeBlock.of(".findAny().orElseThrow(() -> missingRequired($S, $L))",
        enumName().enumConstant(), parameter.getNames());
  }
}
