package net.jbock.compiler;

import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.SuperCommand;
import net.jbock.compiler.command.ParameterMethodValidator;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
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
  private final ParameterMethodValidator parameterMethodValidator;
  private final AbstractMethodsFinder abstractMethodsFactory;

  @Inject
  MethodsFactory(
      SourceElement sourceElement,
      ParameterMethodValidator parameterMethodValidator,
      AbstractMethodsFinder abstractMethodsFactory) {
    this.sourceElement = sourceElement;
    this.parameterMethodValidator = parameterMethodValidator;
    this.abstractMethodsFactory = abstractMethodsFactory;
  }

  public Either<List<ValidationFailure>, AbstractMethods> findAbstractMethods() {
    return abstractMethodsFactory.findRelevantMethods()
        .flatMap(this::validateAtLeastOneAbstractMethod)
        .flatMap(this::validateParameterMethods)
        .map(methods -> methods.stream()
            .map(SourceMethod::create)
            .collect(Collectors.toList()))
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
      return left(Collections.singletonList(sourceElement.fail(message)));
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
      return left(Collections.singletonList(sourceMethods.get(1).fail(message)));
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

  private Either<List<ValidationFailure>, List<ExecutableElement>> validateAtLeastOneAbstractMethod(
      List<ExecutableElement> sourceMethods) {
    if (sourceMethods.isEmpty()) { // javapoet #739
      return left(Collections.singletonList(sourceElement.fail("expecting at least one abstract method")));
    }
    return right(sourceMethods);
  }
}
