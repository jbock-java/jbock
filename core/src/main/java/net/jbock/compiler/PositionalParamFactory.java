package net.jbock.compiler;

import net.jbock.SuperCommand;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.ConverterFinder;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import java.util.List;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class PositionalParamFactory {

  private final ConverterFinder converterFinder;
  private final SourceMethod sourceMethod;
  private final SourceElement sourceElement;
  private final EnumName enumName;
  private final List<ConvertedParameter<PositionalParameter>> alreadyCreated;

  @Inject
  PositionalParamFactory(
      ConverterFinder converterFinder,
      SourceMethod sourceMethod,
      SourceElement sourceElement,
      EnumName enumName,
      List<ConvertedParameter<PositionalParameter>> alreadyCreated) {
    this.converterFinder = converterFinder;
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.enumName = enumName;
    this.alreadyCreated = alreadyCreated;
  }

  public Either<ValidationFailure, ConvertedParameter<PositionalParameter>> createPositionalParam(int position) {
    PositionalParameter positionalParameter = new PositionalParameter(
        sourceMethod,
        enumName,
        position);
    return Either.<String, PositionalParameter>right(positionalParameter)
        .flatMap(coercion -> converterFinder.findConverter(positionalParameter))
        .flatMap(this::checkPositionNotNegative)
        .flatMap(this::checkSuperNotRepeatable)
        .flatMap(this::checkOnlyOnePositionalList)
        .flatMap(this::checkRankConsistentWithPosition)
        .mapLeft(sourceMethod::fail);
  }

  private Either<String, ConvertedParameter<PositionalParameter>> checkOnlyOnePositionalList(
      ConvertedParameter<PositionalParameter> c) {
    if (!c.isRepeatable()) {
      return right(c);
    }
    return Either.maybeLeft(alreadyCreated.stream()
        .filter(ConvertedParameter::isRepeatable)
        .map(p -> "positional parameter " + p.paramLabel() + " is also repeatable")
        .findAny())
        .orRight(() -> c);
  }

  private Either<String, ConvertedParameter<PositionalParameter>> checkPositionNotNegative(
      ConvertedParameter<PositionalParameter> c) {
    PositionalParameter p = c.parameter();
    if (p.position() < 0) {
      return left("negative positions are not allowed");
    }
    return right(c);
  }

  private Either<String, ConvertedParameter<PositionalParameter>> checkSuperNotRepeatable(ConvertedParameter<PositionalParameter> c) {
    if (sourceElement.isSuperCommand() && c.isRepeatable()) {
      return left("in a @" + SuperCommand.class.getSimpleName() +
          ", repeatable params are not supported");
    }
    return right(c);
  }

  private Either<String, ConvertedParameter<PositionalParameter>> checkRankConsistentWithPosition(ConvertedParameter<PositionalParameter> c) {
    PositionalParameter p = c.parameter();
    int thisOrder = c.isRepeatable() ? 2 : c.isOptional() ? 1 : 0;
    int thisPosition = p.position();
    for (ConvertedParameter<PositionalParameter> other : alreadyCreated) {
      int otherOrder = other.isRepeatable() ? 2 : other.isOptional() ? 1 : 0;
      if (thisPosition == other.parameter().position()) {
        return left("duplicate position");
      }
      if (thisOrder > otherOrder && thisPosition < other.parameter().position()) {
        return left("position must be greater than position of " +
            other.skew() + " parameter " + other.paramLabel());
      }
      if (thisOrder < otherOrder && thisPosition > other.parameter().position()) {
        return left("position must be less than position of " +
            other.skew() + " parameter " + other.paramLabel());
      }
    }
    return right(c);
  }
}
