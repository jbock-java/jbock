package net.jbock.validate;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.SuperCommand;
import net.jbock.compiler.SourceElement;
import net.jbock.compiler.ValidationFailure;
import net.jbock.either.Either;
import net.jbock.parameter.ParameterStyle;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
  public Either<List<ValidationFailure>, AbstractMethods> findAbstractMethods() {
    List<ExecutableElement> methods = abstractMethodsFinder.findAbstractMethods();
    return validateParameterMethods(methods)
        .flatMap(this::checkAtLeastOneAbstractMethod)
        .map(this::toSourceMethods)
        .flatMap(this::validateDuplicateParametersAnnotation)
        .flatMap(this::validateParameterSuperCommand);
  }

  private Either<List<ValidationFailure>, AbstractMethods> validateParameterSuperCommand(
      List<SourceMethod> methods) {
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
      return left(List.of(sourceElement.fail(message)));
    }
    return right(new AbstractMethods(params, options));
  }

  private Either<List<ValidationFailure>, List<SourceMethod>> validateDuplicateParametersAnnotation(
      List<SourceMethod> sourceMethods) {
    List<SourceMethod> parametersMethods = sourceMethods.stream()
        .filter(m -> m.style() == ParameterStyle.PARAMETERS)
        .collect(Collectors.toList());
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

  private Either<List<ValidationFailure>, List<ExecutableElement>> checkAtLeastOneAbstractMethod(
      List<ExecutableElement> sourceMethods) {
    if (sourceMethods.isEmpty()) { // javapoet #739
      String message = "expecting at least one abstract method";
      ValidationFailure failure = sourceElement.fail(message);
      return left(List.of(failure));
    }
    return right(sourceMethods);
  }

  private List<SourceMethod> toSourceMethods(List<ExecutableElement> methods) {
    return methods.stream()
        .map(SourceMethod::create)
        .collect(Collectors.toList());
  }
}
