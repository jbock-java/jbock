package net.jbock.convert.matching;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.convert.Skew;
import net.jbock.parameter.ParameterStyle;

import java.util.Optional;

abstract class MatchValidator {

  private final ParameterStyle parameterStyle;

  MatchValidator(ParameterStyle parameterStyle) {
    this.parameterStyle = parameterStyle;
  }

  Optional<String> validateMatch(Match m) {
    if (parameterStyle == ParameterStyle.PARAMETER
        && m.skew() == Skew.REPEATABLE) {
      return Optional.of("use @" + Parameters.class.getSimpleName() + " here");
    }
    if (parameterStyle == ParameterStyle.PARAMETERS
        && m.skew() != Skew.REPEATABLE) {
      return Optional.of("use @" + Parameter.class.getSimpleName() + " here");
    }
    return Optional.empty();
  }
}
