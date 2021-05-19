package net.jbock.compiler;

import net.jbock.Parameter;
import net.jbock.SuperCommand;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class MethodsFactory {

  private static final Comparator<SourceMethod> POSITION_COMPARATOR =
      Comparator.comparingInt(m -> m.index().orElse(Integer.MAX_VALUE));

  private final SourceElement sourceElement;

  @Inject
  MethodsFactory(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  public Either<List<ValidationFailure>, Methods> create(List<SourceMethod> methods) {
    List<SourceMethod> params = methods.stream()
        .filter(m -> m.style().isPositional())
        .sorted(POSITION_COMPARATOR)
        .collect(Collectors.toList());
    List<SourceMethod> options = methods.stream()
        .filter(m -> !m.style().isPositional())
        .collect(Collectors.toList());
    if (sourceElement.isSuperCommand() && params.isEmpty()) {
      String message = "in a @" + SuperCommand.class.getSimpleName() +
          ", at least one @" + Parameter.class.getSimpleName() + " must be defined";
      return left(Collections.singletonList(sourceElement.fail(message)));
    }
    return right(new Methods(params, options));
  }
}
