package net.jbock.compiler;

import net.jbock.SuperCommand;
import net.jbock.coerce.BasicInfo;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.either.Either;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

class PositionalParamFactory extends ParameterScoped {

  private final BasicInfo basicInfo;
  private final ParserFlavour flavour;

  @Inject
  PositionalParamFactory(
      ParameterContext parameterContext,
      BasicInfo basicInfo,
      ParserFlavour flavour) {
    super(parameterContext);
    this.basicInfo = basicInfo;
    this.flavour = flavour;
  }

  Either<ValidationFailure, PositionalParameter> createPositionalParam(int positionalIndex) {
    return basicInfo.coercion()
        .map(coercion -> new PositionalParameter(
            sourceMethod(),
            bundleKey(),
            coercion,
            Arrays.asList(description()),
            positionalIndex))
        .filter(this::checkPositionNotNegative)
        .filter(this::checkSuperNotRepeatable)
        .filter(this::checkOnlyOnePositionalList)
        .filter(this::checkRankConsistentWithPosition)
        .mapLeft(s -> new ValidationFailure(s, sourceMethod()));
  }

  private Optional<String> checkOnlyOnePositionalList(PositionalParameter param) {
    if (!param.isRepeatable()) {
      return Optional.empty();
    }
    return alreadyCreatedParams().stream()
        .filter(p -> p.isRepeatable() && p.isPositional())
        .map(p -> "positional parameter " + p.enumConstant() + " is also repeatable")
        .findAny();
  }

  private Optional<String> checkPositionNotNegative(PositionalParameter p) {
    if (p.position() < 0) {
      return Optional.of("negative positions are not allowed");
    }
    return Optional.empty();
  }

  private Optional<String> checkSuperNotRepeatable(PositionalParameter p) {
    if (flavour.isSuperCommand() && p.isRepeatable()) {
      return Optional.of("in a @" + SuperCommand.class.getSimpleName() +
          ", repeatable params are not supported");
    }
    return Optional.empty();
  }

  private Optional<String> checkRankConsistentWithPosition(PositionalParameter p) {
    int thisOrder = p.isRepeatable() ? 2 : p.isOptional() ? 1 : 0;
    int thisPosition = p.position();
    List<PositionalParameter> allPositional = alreadyCreatedParams();
    for (PositionalParameter other : allPositional) {
      int otherOrder = other.isRepeatable() ? 2 : other.isOptional() ? 1 : 0;
      if (thisPosition == other.position()) {
        return Optional.of("duplicate position");
      }
      if (thisOrder > otherOrder && thisPosition < other.position()) {
        return Optional.of("position must be greater than position of " +
            other.coercion().skew() + " parameter " + other.enumName());
      }
      if (thisOrder < otherOrder && thisPosition > other.position()) {
        return Optional.of("position must be less than position of " +
            other.coercion().skew() + " parameter " + other.enumName());
      }
    }
    return Optional.empty();
  }
}
