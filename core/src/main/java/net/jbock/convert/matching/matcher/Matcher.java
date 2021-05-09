package net.jbock.convert.matching.matcher;

import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.matching.Match;

import java.util.Optional;

public abstract class Matcher extends ParameterScoped {

  Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  public abstract Optional<Match> tryMatch(AbstractParameter parameter);
}
