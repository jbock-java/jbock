package net.jbock.compiler;

import net.jbock.compiler.command.AllMethodsFinder;
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

  public Either<List<ValidationFailure>, List<ExecutableElement>> findRelevantMethods() {
    return allMethodsFinder.findMethodsInSourceElement()
        .map(acc -> acc.stream()
            .collect(partitioningBy(m -> m.getModifiers().contains(ABSTRACT))))
        .map(partitions -> {
          List<ExecutableElement> abstractMethods = partitions.get(true);
          Map<Name, List<ExecutableElement>> nonAbstractMethods = partitions.get(false)
              .stream()
              .collect(groupingBy(ExecutableElement::getSimpleName));
          return new AbstractMethodsUtil(abstractMethods, nonAbstractMethods, types)
              .findRelevantAbstractMethods();
        });
  }
}
