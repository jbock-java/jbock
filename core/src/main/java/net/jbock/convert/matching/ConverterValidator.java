package net.jbock.convert.matching;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.convert.Skew;

import java.util.Optional;

public abstract class ConverterValidator {

  private final ParameterStyle parameterStyle;

  protected ConverterValidator(ParameterStyle parameterStyle) {
    this.parameterStyle = parameterStyle;
  }

  protected Optional<String> validateMatch(Match m) {
    if (parameterStyle == ParameterStyle.PARAMETER
        && m.skew() == Skew.REPEATABLE) {
      return Optional.of("use @" + Parameters.class.getSimpleName() + " here");
    }
    if (parameterStyle == ParameterStyle.PARAMETERS
        && (m.skew() == Skew.REQUIRED || m.skew() == Skew.OPTIONAL)) {
      return Optional.of("use @" + Parameter.class.getSimpleName() + " here");
    }
    return Optional.empty();
  }
}
