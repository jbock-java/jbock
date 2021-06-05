package net.jbock.validate;

import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapped;
import net.jbock.either.Either;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

@ValidateScope
public class ParamsFactory {

  private final SourceElement sourceElement;
  private final Util util;

  @Inject
  ParamsFactory(SourceElement sourceElement, Util util) {
    this.sourceElement = sourceElement;
    this.util = util;
  }

  public Either<List<ValidationFailure>, Items> create(
      List<Mapped<PositionalParameter>> positionalParams,
      List<Mapped<NamedOption>> namedOptions) {
    List<ValidationFailure> failures = checkDuplicateDescriptionKeys(namedOptions, positionalParams);
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return right(new Items(positionalParams, namedOptions));
  }

  private List<ValidationFailure> checkDuplicateDescriptionKeys(
      List<Mapped<NamedOption>> namedOptions,
      List<Mapped<PositionalParameter>> positionalParams) {
    List<ValidationFailure> failures = new ArrayList<>();
    List<Mapped<? extends AbstractItem>> abstractParameters =
        util.concat(namedOptions, positionalParams);
    Set<String> keys = new HashSet<>();
    sourceElement.descriptionKey().ifPresent(keys::add);
    for (Mapped<? extends AbstractItem> c : abstractParameters) {
      AbstractItem p = c.item();
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
