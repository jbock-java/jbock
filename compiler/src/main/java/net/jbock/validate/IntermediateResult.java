package net.jbock.validate;

import net.jbock.common.ValidationFailure;
import net.jbock.convert.ConvertedParameter;
import net.jbock.either.Either;
import net.jbock.parameter.PositionalParameter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class IntermediateResult {

  private final List<SourceMethod> namedOptions;
  private final List<ConvertedParameter<PositionalParameter>> positionalParameters;

  private IntermediateResult(
      List<SourceMethod> namedOptions,
      List<ConvertedParameter<PositionalParameter>> positionalParameters) {
    this.namedOptions = namedOptions;
    this.positionalParameters = positionalParameters;
  }

  public static Either<List<ValidationFailure>, IntermediateResult> create(
      List<SourceMethod> namedOptions,
      List<ConvertedParameter<PositionalParameter>> positionalParameters) {
    List<ValidationFailure> failures = validatePositions(positionalParameters);
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return right(new IntermediateResult(namedOptions, positionalParameters));
  }

  private static List<ValidationFailure> validatePositions(
      List<ConvertedParameter<PositionalParameter>> params) {
    List<ConvertedParameter<PositionalParameter>> sorted = params.stream()
        .sorted(Comparator.comparing(c -> c.parameter().position()))
        .collect(Collectors.toUnmodifiableList());
    List<ValidationFailure> failures = new ArrayList<>();
    for (int i = 0; i < sorted.size(); i++) {
      ConvertedParameter<PositionalParameter> c = sorted.get(i);
      PositionalParameter p = c.parameter();
      if (p.position() != i) {
        String message = "Position " + p.position() + " is not available. Suggested position: " + i;
        failures.add(p.fail(message));
      }
    }
    return failures;
  }

  public List<SourceMethod> options() {
    return namedOptions;
  }

  public List<ConvertedParameter<PositionalParameter>> positionalParameters() {
    return positionalParameters;
  }
}
