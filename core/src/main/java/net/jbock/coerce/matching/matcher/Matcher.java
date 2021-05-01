package net.jbock.coerce.matching.matcher;

import net.jbock.coerce.matching.Match;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.parameter.Parameter;

import java.util.Optional;

public abstract class Matcher extends ParameterScoped {

  public Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  public abstract Optional<Match> tryMatch(Parameter parameter);
}
