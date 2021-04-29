package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import net.jbock.SuperCommand;
import net.jbock.coerce.BasicInfo;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.either.Either;

import javax.inject.Inject;
import java.util.Arrays;
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

  Either<ValidationFailure, Coercion<PositionalParameter>> createPositionalParam(int positionalIndex) {
    PositionalParameter positionalParameter = new PositionalParameter(
        sourceMethod(),
        bundleKey(),
        Arrays.asList(description()),
        positionalIndex);
    return Either.<String, PositionalParameter>right(positionalParameter)
        .flatMap(coercion -> basicInfo.coercion(positionalParameter))
        .filter(this::checkPositionNotNegative)
        .filter(this::checkSuperNotRepeatable)
        .filter(this::checkOnlyOnePositionalList)
        .filter(this::checkRankConsistentWithPosition)
        .mapLeft(s -> new ValidationFailure(s, sourceMethod()));
  }

  private Optional<String> checkOnlyOnePositionalList(Coercion<PositionalParameter> c) {
    if (!c.isRepeatable()) {
      return Optional.empty();
    }
    return alreadyCreatedParams().stream()
        .filter(Coercion::isRepeatable)
        .map(p -> "positional parameter " + p.enumName().enumConstant() + " is also repeatable")
        .findAny();
  }

  private Optional<String> checkPositionNotNegative(Coercion<PositionalParameter> c) {
    PositionalParameter p = c.parameter();
    if (p.position() < 0) {
      return Optional.of("negative positions are not allowed");
    }
    return Optional.empty();
  }

  private Optional<String> checkSuperNotRepeatable(Coercion<PositionalParameter> c) {
    if (flavour.isSuperCommand() && c.isRepeatable()) {
      return Optional.of("in a @" + SuperCommand.class.getSimpleName() +
          ", repeatable params are not supported");
    }
    return Optional.empty();
  }

  private Optional<String> checkRankConsistentWithPosition(Coercion<PositionalParameter> c) {
    PositionalParameter p = c.parameter();
    int thisOrder = c.isRepeatable() ? 2 : c.isOptional() ? 1 : 0;
    int thisPosition = p.position();
    ImmutableList<Coercion<PositionalParameter>> allPositional = alreadyCreatedParams();
    for (Coercion<PositionalParameter> other : allPositional) {
      int otherOrder = other.isRepeatable() ? 2 : other.isOptional() ? 1 : 0;
      if (thisPosition == other.parameter().position()) {
        return Optional.of("duplicate position");
      }
      if (thisOrder > otherOrder && thisPosition < other.parameter().position()) {
        return Optional.of("position must be greater than position of " +
            other.skew() + " parameter " + other.enumName());
      }
      if (thisOrder < otherOrder && thisPosition > other.parameter().position()) {
        return Optional.of("position must be less than position of " +
            other.skew() + " parameter " + other.enumName());
      }
    }
    return Optional.empty();
  }
}
