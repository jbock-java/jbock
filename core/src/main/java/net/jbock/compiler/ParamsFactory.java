package net.jbock.compiler;

import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class ParamsFactory {

  private final SourceElement sourceElement;
  private final Util util;

  @Inject
  ParamsFactory(SourceElement sourceElement, Util util) {
    this.sourceElement = sourceElement;
    this.util = util;
  }

  public Either<List<ValidationFailure>, Params> create(
      List<ConvertedParameter<PositionalParameter>> positionalParams,
      List<ConvertedParameter<NamedOption>> namedOptions) {
    List<ValidationFailure> failures = checkDuplicateDescriptionKeys(namedOptions, positionalParams);
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return right(new Params(positionalParams, namedOptions));
  }

  private List<ValidationFailure> checkDuplicateDescriptionKeys(
      List<ConvertedParameter<NamedOption>> namedOptions,
      List<ConvertedParameter<PositionalParameter>> positionalParams) {
    List<ValidationFailure> failures = new ArrayList<>();
    List<ConvertedParameter<? extends AbstractParameter>> abstractParameters =
        util.concat(namedOptions, positionalParams);
    Set<String> keys = new HashSet<>();
    sourceElement.descriptionKey().ifPresent(keys::add);
    for (ConvertedParameter<? extends AbstractParameter> c : abstractParameters) {
      AbstractParameter p = c.parameter();
      String key = p.descriptionKey().orElse("");
      if (key.isEmpty()) {
        continue;
      }
      if (!keys.add(key)) {
        String message = "duplicate description key: " + key;
        failures.add(p.fail(message));
      }
    }
    return failures;
  }
}
