package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.convert.Skew;
import net.jbock.convert.matching.Match;

import javax.inject.Inject;
import java.util.Optional;

public class ExactMatcher extends Matcher {

  @Inject
  ExactMatcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  @Override
  public Optional<Match> tryMatch(AbstractParameter parameter) {
    if (parameter.style() == ParameterStyle.PARAMETERS) {
      // @Parameters doesn't do required
      return Optional.empty();
    }
    ParameterSpec constructorParam = constructorParam(boxedReturnType());
    Match match = Match.create(boxedReturnType(), constructorParam, Skew.REQUIRED);
    return Optional.of(match);
  }
}
