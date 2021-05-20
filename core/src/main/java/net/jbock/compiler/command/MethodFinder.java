package net.jbock.compiler.command;

import net.jbock.compiler.ValidationFailure;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.compiler.TypeTool.AS_TYPE_ELEMENT;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class MethodFinder {

  private final SourceElement sourceElement;

  @Inject
  MethodFinder(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  public Either<List<ValidationFailure>, List<ExecutableElement>> findRelevantMethods() {
    return findRelevantMethods(sourceElement.element().asType());
  }

  private Either<List<ValidationFailure>, List<ExecutableElement>> findRelevantMethods(TypeMirror sourceElement) {
    List<ExecutableElement> acc = new ArrayList<>();
    while (true) {
      Either<List<ValidationFailure>, TypeElement> element = findRelevantMethods(sourceElement, acc);
      if (!element.isRight()) {
        List<ValidationFailure> failures = element.fold(
            Function.identity(),
            __ -> Collections.emptyList());
        if (!failures.isEmpty()) {
          return left(failures);
        } else {
          return right(acc);
        }
      }
      TypeElement folded = element.fold(
          __ -> null, // doesn't happen
          Function.identity());
      sourceElement = folded.getSuperclass();
    }
  }

  private Either<List<ValidationFailure>, TypeElement> findRelevantMethods(
      TypeMirror sourceElement, List<ExecutableElement> acc) {
    if (sourceElement.getKind() != TypeKind.DECLARED) {
      return left(Collections.emptyList());
    }
    DeclaredType declared = AS_DECLARED.visit(sourceElement);
    if (declared == null) {
      return left(Collections.emptyList());
    }
    TypeElement typeElement = AS_TYPE_ELEMENT.visit(declared.asElement());
    if (typeElement == null) {
      return left(Collections.emptyList());
    }
    if (!typeElement.getModifiers().contains(ABSTRACT)) {
      return left(Collections.emptyList());
    }
    List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
    if (!interfaces.isEmpty()) {
      return left(Collections.singletonList(
          new ValidationFailure("this abstract class may not implement any interfaces", typeElement)));
    }
    acc.addAll(methodsIn(typeElement.getEnclosedElements()));
    return right(typeElement);
  }
}
