package net.jbock.convert;

import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.either.Either;
import net.jbock.either.LeftOptional;
import net.jbock.parameter.PositionalParameter;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.List;
import java.util.Locale;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

@ParameterScope
public class PositionalParamFactory {

  private final ConverterFinder converterFinder;
  private final SourceMethod sourceMethod;
  private final SourceElement sourceElement;
  private final EnumName enumName;
  private final List<Mapped<PositionalParameter>> alreadyCreated;

  @Inject
  PositionalParamFactory(
      ConverterFinder converterFinder,
      SourceMethod sourceMethod,
      SourceElement sourceElement,
      EnumName enumName,
      List<Mapped<PositionalParameter>> alreadyCreated) {
    this.converterFinder = converterFinder;
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.enumName = enumName;
    this.alreadyCreated = alreadyCreated;
  }

  public Either<ValidationFailure, Mapped<PositionalParameter>> createPositionalParam(int position) {
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

  private Either<String, Mapped<PositionalParameter>> checkOnlyOnePositionalList(
      Mapped<PositionalParameter> c) {
    if (!c.isRepeatable()) {
      return right(c);
    }
    return alreadyCreated.stream()
        .filter(Mapped::isRepeatable)
        .map(p -> "positional parameter " + p.paramLabel() + " is also repeatable")
        .findAny()
        .map(LeftOptional::of)
        .orElse(LeftOptional.empty())
        .orElseRight(() -> c);
  }

  private Either<String, Mapped<PositionalParameter>> checkPositionNotNegative(
      Mapped<PositionalParameter> c) {
    PositionalParameter p = c.item();
    if (p.position() < 0) {
      return left("negative positions are not allowed");
    }
    return right(c);
  }

  private Either<String, Mapped<PositionalParameter>> checkSuperNotRepeatable(Mapped<PositionalParameter> c) {
    if (sourceElement.isSuperCommand() && c.isRepeatable()) {
      return left("positional parameter may not be repeatable when the superCommand" +
          " attribute is set");
    }
    return right(c);
  }

  private Either<String, Mapped<PositionalParameter>> checkRankConsistentWithPosition(Mapped<PositionalParameter> c) {
    PositionalParameter p = c.item();
    int thisOrder = c.isRepeatable() ? 2 : c.isOptional() ? 1 : 0;
    int thisPosition = p.position();
    for (Mapped<PositionalParameter> other : alreadyCreated) {
      int otherOrder = other.isRepeatable() ? 2 : other.isOptional() ? 1 : 0;
      if (thisPosition == other.item().position()) {
        return left("duplicate position");
      }
      if (thisOrder > otherOrder && thisPosition < other.item().position()) {
        return left("position must be greater than position of " +
            other.multiplicity().name().toLowerCase(Locale.US) +
            " parameter " + other.paramLabel());
      }
      if (thisOrder < otherOrder && thisPosition > other.item().position()) {
        return left("position must be less than position of " +
            other.multiplicity().name().toLowerCase(Locale.US) +
            " parameter " + other.paramLabel());
      }
    }
    return right(c);
  }
}
