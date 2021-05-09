package net.jbock.convert.matching;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.convert.Skew;

import java.util.Optional;

public abstract class ConverterFinder extends ParameterScoped {

  protected ConverterFinder(ParameterContext parameterContext) {
    super(parameterContext);
  }

  protected Optional<String> validateMatch(AbstractParameter parameter, Match m) {
    if (parameter.style() == ParameterStyle.PARAMETER
        && m.skew() == Skew.REPEATABLE) {
      return Optional.of("use @" + Parameters.class.getSimpleName() + " here");
    }
    if (parameter.style() == ParameterStyle.PARAMETERS
        && (m.skew() == Skew.REQUIRED || m.skew() == Skew.OPTIONAL)) {
      return Optional.of("use @" + Parameter.class.getSimpleName() + " here");
    }
    return Optional.empty();
  }
}
