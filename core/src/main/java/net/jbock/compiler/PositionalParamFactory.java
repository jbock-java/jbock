package net.jbock.compiler;

import net.jbock.SuperCommand;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.ConverterFinder;
import net.jbock.either.Either;
import net.jbock.qualifier.DescriptionKey;
import net.jbock.qualifier.ParamLabel;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

class PositionalParamFactory {

  private final ConverterFinder converterFinder;
  private final ParamLabel paramLabel;
  private final DescriptionKey descriptionKey;
  private final SourceMethod sourceMethod;
  private final SourceElement sourceElement;
  private final EnumName enumName;
  private final Description description;
  private final List<ConvertedParameter<PositionalParameter>> alreadyCreated;

  @Inject
  PositionalParamFactory(
      ConverterFinder converterFinder,
      ParamLabel paramLabel,
      DescriptionKey descriptionKey,
      SourceMethod sourceMethod,
      SourceElement sourceElement, EnumName enumName,
      Description description,
      List<ConvertedParameter<PositionalParameter>> alreadyCreated) {
    this.converterFinder = converterFinder;
    this.paramLabel = paramLabel;
    this.descriptionKey = descriptionKey;
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.enumName = enumName;
    this.description = description;
    this.alreadyCreated = alreadyCreated;
  }

  Either<ValidationFailure, ConvertedParameter<PositionalParameter>> createPositionalParam(int position) {
    PositionalParameter positionalParameter = new PositionalParameter(
        sourceMethod,
        enumName,
        descriptionKey,
        description,
        position,
        paramLabel);
    return Either.<String, PositionalParameter>right(positionalParameter)
        .flatMap(coercion -> converterFinder.findConverter(positionalParameter))
        .filter(this::checkPositionNotNegative)
        .filter(this::checkSuperNotRepeatable)
        .filter(this::checkOnlyOnePositionalList)
        .filter(this::checkRankConsistentWithPosition)
        .mapLeft(sourceMethod::fail);
  }

  private Optional<String> checkOnlyOnePositionalList(ConvertedParameter<PositionalParameter> c) {
    if (!c.isRepeatable()) {
      return Optional.empty();
    }
    return alreadyCreated.stream()
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
    if (sourceElement.isSuperCommand() && c.isRepeatable()) {
      return Optional.of("in a @" + SuperCommand.class.getSimpleName() +
          ", repeatable params are not supported");
    }
    return Optional.empty();
  }

  private Optional<String> checkRankConsistentWithPosition(ConvertedParameter<PositionalParameter> c) {
    PositionalParameter p = c.parameter();
    int thisOrder = c.isRepeatable() ? 2 : c.isOptional() ? 1 : 0;
    int thisPosition = p.position();
    for (ConvertedParameter<PositionalParameter> other : alreadyCreated) {
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
