package net.jbock.compiler;

import net.jbock.coerce.BasicInfo;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.either.Either;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

class PositionalParamFactory extends ParameterScoped {

  private final BasicInfo basicInfo;

  @Inject
  PositionalParamFactory(ParameterContext parameterContext, BasicInfo basicInfo) {
    super(parameterContext);
    this.basicInfo = basicInfo;
  }

  Either<ValidationFailure, ? extends Parameter> createPositionalParam(int positionalIndex) {
    return Either.<String, Void>fromOptionalFailure(checkBundleKey())
        .select(() -> basicInfo.coercion()
            .map(coercion -> new PositionalParameter(
                sourceMethod(),
                bundleKey(),
                enumName().snake().toLowerCase(Locale.US),
                coercion,
                Arrays.asList(description()),
                positionalIndex)))
        .filter(this::checkOnlyOnePositionalList)
        .filter(this::checkRankConsistentWithPosition)
        .mapLeft(s -> new ValidationFailure(s, sourceMethod()));
  }

  private Optional<String> checkOnlyOnePositionalList(PositionalParameter param) {
    if (!param.isRepeatable()) {
      return Optional.empty();
    }
    return alreadyCreated().stream()
        .filter(p -> p.isRepeatable() && p.isPositional())
        .map(p -> "positional parameter " + p.enumConstant() + " is also repeatable")
        .findAny();
  }

  private Optional<String> checkRankConsistentWithPosition(PositionalParameter p) {
    int thisOrder = p.isRepeatable() ? 2 : p.isOptional() ? 1 : 0;
    int thisPosition = p.position();
    List<PositionalParameter> allPositional = alreadyCreated().stream().filter(Parameter::isPositional)
        .map(parameter -> (PositionalParameter) parameter)
        .collect(Collectors.toList());
    for (PositionalParameter other : allPositional) {
      int otherOrder = other.isRepeatable() ? 2 : other.isOptional() ? 1 : 0;
      if (thisPosition == other.position()) {
        return Optional.of("duplicate position");
      }
      if (thisOrder > otherOrder && thisPosition < other.position()) {
        return Optional.of("position must be greater than position of " +
            other.coercion().skew() + " parameter " + other.paramName());
      }
      if (thisOrder < otherOrder && thisPosition > other.position()) {
        return Optional.of("position must be less than position of " +
            other.coercion().skew() + " parameter " + other.paramName());
      }
    }
    return Optional.empty();
  }
}
