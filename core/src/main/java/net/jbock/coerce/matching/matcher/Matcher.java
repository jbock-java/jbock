package net.jbock.coerce.matching.matcher;

import net.jbock.coerce.matching.Match;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.parameter.AbstractParameter;

import java.util.Optional;

public abstract class Matcher extends ParameterScoped {

  Matcher(ParameterContext parameterContext) {
    super(parameterContext);
  }

  public abstract Optional<Match> tryMatch(AbstractParameter parameter);
}
