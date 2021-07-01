package net.jbock.convert.matching;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.either.LeftOptional;
import net.jbock.model.Multiplicity;
import net.jbock.validate.ParameterStyle;

abstract class MatchValidator {

  private final ParameterStyle parameterStyle;

  MatchValidator(ParameterStyle parameterStyle) {
    this.parameterStyle = parameterStyle;
  }

  LeftOptional<String> validateMatch(Match m) {
    if (parameterStyle == ParameterStyle.PARAMETER
        && m.multiplicity() == Multiplicity.REPEATABLE) {
      return LeftOptional.of("use @" + Parameters.class.getSimpleName() + " here");
    }
    if (parameterStyle == ParameterStyle.PARAMETERS
        && m.multiplicity() != Multiplicity.REPEATABLE) {
      return LeftOptional.of("use @" + Parameter.class.getSimpleName() + " here");
    }
    return LeftOptional.empty();
  }
}
