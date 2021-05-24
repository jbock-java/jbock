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
import java.util.List;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.compiler.TypeTool.AS_TYPE_ELEMENT;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class AllMethodsFinder {

  private final SourceElement sourceElement;

  @Inject
  AllMethodsFinder(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  /**
   * find methods in source element, including inherited
   */
  public Either<List<ValidationFailure>, List<ExecutableElement>> findMethodsInSourceElement() {
    TypeMirror mirror = sourceElement.element().asType();
    return findMethodsIn(mirror);
  }

  private Either<List<ValidationFailure>, List<ExecutableElement>> findMethodsIn(
      TypeMirror mirror) {
    List<ExecutableElement> acc = new ArrayList<>();
    while (true) {
      Either<List<ValidationFailure>, TypeElement> element = findMethodsIn(mirror, acc);
      if (!element.isRight()) {
        List<ValidationFailure> failures = element.fold(
            Function.identity(),
            __ -> List.of());
        if (!failures.isEmpty()) {
          return left(failures);
        } else {
          return right(acc);
        }
      }
      TypeElement folded = element.fold(
          __ -> null, // doesn't happen
          Function.identity());
      mirror = folded.getSuperclass();
    }
  }

  private Either<List<ValidationFailure>, TypeElement> findMethodsIn(
      TypeMirror mirror, List<ExecutableElement> acc) {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return left(List.of());
    }
    DeclaredType declared = AS_DECLARED.visit(mirror);
    if (declared == null) {
      return left(List.of());
    }
    TypeElement typeElement = AS_TYPE_ELEMENT.visit(declared.asElement());
    if (typeElement == null) {
      return left(List.of());
    }
    if (!typeElement.getModifiers().contains(ABSTRACT)) {
      return left(List.of());
    }
    List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
    if (!interfaces.isEmpty()) {
      return left(List.of(
          new ValidationFailure("this abstract class may not implement any interfaces", typeElement)));
    }
    acc.addAll(methodsIn(typeElement.getEnclosedElements()));
    return right(typeElement);
  }
}
