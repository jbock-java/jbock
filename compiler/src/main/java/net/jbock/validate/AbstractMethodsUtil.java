package net.jbock.validate;

import net.jbock.common.Annotations;
import net.jbock.common.ValidationFailure;
import net.jbock.either.Either;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

class AbstractMethodsUtil {

  private final Map<Name, List<ExecutableElement>> nonAbstractMethods;
  private final Types types;

  AbstractMethodsUtil(
      Map<Name, List<ExecutableElement>> nonAbstractMethods,
      Types types) {
    this.nonAbstractMethods = nonAbstractMethods;
    this.types = types;
  }

  /**
   * Find abstract methods that are not implemented
   * further below in the hierarchy.
   * An overridden method may not be annotated.
   */
  Either<List<ValidationFailure>, List<ExecutableElement>> findRelevantAbstractMethods(
      List<ExecutableElement> abstractMethods) {
    Map<Boolean, List<ExecutableElement>> partition = abstractMethods.stream()
        .collect(Collectors.partitioningBy(this::isNotOverridden));
    List<ExecutableElement> notOverridden = partition.get(true);
    List<ExecutableElement> overridden = partition.get(false);
    List<ValidationFailure> failures = overridden.stream().flatMap(m -> {
      for (Class<? extends Annotation> ann : Annotations.methodLevelAnnotations()) {
        if (m.getAnnotation(ann) != null) {
          return Stream.of(m);
        }
      }
      return Stream.empty();
    }).map(m -> new ValidationFailure("annotated method is overridden", m))
        .collect(Collectors.toUnmodifiableList());
    if (!failures.isEmpty()) {
      return left(failures);
    }
    return right(notOverridden);
  }

  private boolean isNotOverridden(ExecutableElement abstractMethod) {
    Name name = abstractMethod.getSimpleName();
    List<ExecutableElement> methodsByTheSameName = nonAbstractMethods.get(name);
    if (methodsByTheSameName == null) {
      return true;
    }
    return methodsByTheSameName.stream()
        .noneMatch(method -> isSameSignature(method, abstractMethod));
  }

  private boolean isSameSignature(ExecutableElement m1, ExecutableElement m2) {
    if (m1.getParameters().size() != m2.getParameters().size()) {
      return false;
    }
    for (int i = 0; i < m1.getParameters().size(); i++) {
      VariableElement p1 = m1.getParameters().get(i);
      VariableElement p2 = m2.getParameters().get(i);
      if (!types.isSameType(p1.asType(), p2.asType())) {
        return false;
      }
    }
    return true;
  }
}
