package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import net.jbock.SuperCommand;
import net.jbock.convert.BasicInfo;
import net.jbock.convert.ConvertedParameter;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.either.Either;
import net.jbock.qualifier.ConverterClass;

import javax.inject.Inject;
import java.util.Optional;

class PositionalParamFactory extends ParameterScoped {

  private final BasicInfo basicInfo;
  private final ParserFlavour flavour;
  private final ConverterClass converter;

  @Inject
  PositionalParamFactory(
      ParameterContext parameterContext,
      BasicInfo basicInfo,
      ParserFlavour flavour,
      ConverterClass converter) {
    super(parameterContext);
    this.basicInfo = basicInfo;
    this.flavour = flavour;
    this.converter = converter;
  }

  Either<ValidationFailure, ConvertedParameter<PositionalParameter>> createPositionalParam(int positionalIndex) {
    PositionalParameter positionalParameter = new PositionalParameter(
        sourceMethod(),
        enumName(),
        descriptionKey(),
        description(),
        positionalIndex,
        converter);
    return Either.<String, PositionalParameter>right(positionalParameter)
        .flatMap(coercion -> basicInfo.coercion(positionalParameter))
        .filter(this::checkPositionNotNegative)
        .filter(this::checkSuperNotRepeatable)
        .filter(this::checkOnlyOnePositionalList)
        .filter(this::checkRankConsistentWithPosition)
        .mapLeft(s -> new ValidationFailure(s, sourceMethod()));
  }

  private Optional<String> checkOnlyOnePositionalList(ConvertedParameter<PositionalParameter> c) {
    if (!c.isRepeatable()) {
      return Optional.empty();
    }
    return alreadyCreatedParams().stream()
        .filter(ConvertedParameter::isRepeatable)
        .map(p -> "positional parameter " + p.enumName().enumConstant() + " is also repeatable")
        .findAny();
  }

  private Optional<String> checkPositionNotNegative(ConvertedParameter<PositionalParameter> c) {
    PositionalParameter p = c.parameter();
    if (p.position() < 0) {
      return Optional.of("negative positions are not allowed");
    }
    return Optional.empty();
  }

  private Optional<String> checkSuperNotRepeatable(ConvertedParameter<PositionalParameter> c) {
    if (flavour.isSuperCommand() && c.isRepeatable()) {
      return Optional.of("in a @" + SuperCommand.class.getSimpleName() +
          ", repeatable params are not supported");
    }
    return Optional.empty();
  }

  private Optional<String> checkRankConsistentWithPosition(ConvertedParameter<PositionalParameter> c) {
    PositionalParameter p = c.parameter();
    int thisOrder = c.isRepeatable() ? 2 : c.isOptional() ? 1 : 0;
    int thisPosition = p.position();
    ImmutableList<ConvertedParameter<PositionalParameter>> allPositional = alreadyCreatedParams();
    for (ConvertedParameter<PositionalParameter> other : allPositional) {
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
