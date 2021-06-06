package net.jbock.validate;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.SuperCommand;
import net.jbock.common.ValidationFailure;
import net.jbock.either.Either;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

@ValidateScope
public class MethodsFactory {

  private static final Comparator<SourceMethod> POSITION_COMPARATOR =
      Comparator.comparingInt(m -> m.index().orElse(Integer.MAX_VALUE));

  private final SourceElement sourceElement;
  private final ParameterMethodValidator parameterMethodValidator;
  private final AbstractMethodsFinder abstractMethodsFinder;

  @Inject
  MethodsFactory(
      SourceElement sourceElement,
      ParameterMethodValidator parameterMethodValidator,
      AbstractMethodsFinder abstractMethodsFinder) {
    this.sourceElement = sourceElement;
    this.parameterMethodValidator = parameterMethodValidator;
    this.abstractMethodsFinder = abstractMethodsFinder;
  }

  /**
   * find unimplemented abstract methods in {@code sourceElement} and its ancestors
   */
  Either<List<ValidationFailure>, AbstractMethods> findAbstractMethods() {
    return abstractMethodsFinder.findAbstractMethods()
        .flatMap(this::validateParameterMethods)
        .flatMap(this::inheritanceCollision)
        .map(this::createSourceMethods)
        .flatMap(this::validateDuplicateParametersAnnotation)
        .flatMap(this::atLeastOneParameterSuperCommand);
  }

  private Either<List<ValidationFailure>, List<ExecutableElement>> inheritanceCollision(
      List<ExecutableElement> methods) {
    Map<Name, List<ExecutableElement>> map = methods.stream().collect(Collectors.groupingBy(ExecutableElement::getSimpleName));
    for (ExecutableElement method : methods) {
      if (map.get(method.getSimpleName()).size() >= 2) {
        return left(List.of(new ValidationFailure("inheritance collision", method)));
      }
    }
    return right(methods);
  }

  private Either<List<ValidationFailure>, AbstractMethods> atLeastOneParameterSuperCommand(
      List<SourceMethod> methods) {
    List<SourceMethod> params = methods.stream()
        .filter(m -> m.style().isPositional())
        .sorted(POSITION_COMPARATOR)
        .collect(Collectors.toUnmodifiableList());
    List<SourceMethod> options = methods.stream()
        .filter(m -> !m.style().isPositional())
        .collect(Collectors.toUnmodifiableList());
    if (sourceElement.isSuperCommand() && params.isEmpty()) {
      String message = "in a @" + SuperCommand.class.getSimpleName() +
          ", at least one @" + Parameter.class.getSimpleName() + " must be defined";
      return left(List.of(sourceElement.fail(message)));
    }
    return right(new AbstractMethods(params, options));
  }

  private Either<List<ValidationFailure>, List<SourceMethod>> validateDuplicateParametersAnnotation(
      List<SourceMethod> sourceMethods) {
    List<SourceMethod> parametersMethods = sourceMethods.stream()
        .filter(m -> m.style() == ParameterStyle.PARAMETERS)
        .collect(Collectors.toUnmodifiableList());
    if (parametersMethods.size() >= 2) {
      String message = "duplicate @" + Parameters.class.getSimpleName() + " annotation";
      return left(List.of(sourceMethods.get(1).fail(message)));
    }
    return right(sourceMethods);
  }

  private Either<List<ValidationFailure>, List<ExecutableElement>> validateParameterMethods(
      List<ExecutableElement> sourceMethods) {
    List<ValidationFailure> failures = new ArrayList<>();
    for (ExecutableElement sourceMethod : sourceMethods) {
      parameterMethodValidator.validateParameterMethod(sourceMethod)
          .map(msg -> new ValidationFailure(msg, sourceMethod))
          .ifPresent(failures::add);
    }
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return right(sourceMethods);
  }

  private List<SourceMethod> createSourceMethods(List<ExecutableElement> methods) {
    return methods.stream()
        .map(SourceMethod::create)
        .collect(Collectors.toUnmodifiableList());
  }
}
