package net.jbock.validate;

import net.jbock.common.ValidationFailure;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static javax.lang.model.element.Modifier.ABSTRACT;

@ValidateScope
public class AbstractMethodsFinder {

  private final Types types;
  private final AllMethodsFinder allMethodsFinder;

  @Inject
  AbstractMethodsFinder(
      Types types,
      AllMethodsFinder allMethodsFinder) {
    this.types = types;
    this.allMethodsFinder = allMethodsFinder;
  }

  public Either<List<ValidationFailure>, List<ExecutableElement>> findAbstractMethods() {
    List<ExecutableElement> methods = allMethodsFinder.findMethodsInSourceElement();
    Map<Boolean, List<ExecutableElement>> partitions = methods.stream()
        .collect(partitioningBy(m -> m.getModifiers().contains(ABSTRACT)));
    List<ExecutableElement> abstractMethods = partitions.get(true);
    Map<Name, List<ExecutableElement>> nonAbstractMethods = partitions.get(false)
        .stream()
        .collect(groupingBy(ExecutableElement::getSimpleName));
    return new AbstractMethodsUtil(nonAbstractMethods, types)
        .findRelevantAbstractMethods(abstractMethods);
  }
}
