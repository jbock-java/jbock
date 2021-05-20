package net.jbock.compiler;

import net.jbock.compiler.command.MethodFinder;
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

public class AbstractMethodsFactory {

  private final Types types;
  private final MethodFinder methodFinder;

  @Inject
  AbstractMethodsFactory(
      Types types,
      MethodFinder methodFinder) {
    this.types = types;
    this.methodFinder = methodFinder;
  }

  public Either<List<ValidationFailure>, List<ExecutableElement>> findRelevantMethods() {
    return methodFinder.findRelevantMethods()
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
