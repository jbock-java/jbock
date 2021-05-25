package net.jbock.compiler.command;

import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.compiler.TypeTool.AS_DECLARED;
import static net.jbock.compiler.TypeTool.AS_TYPE_ELEMENT;

public class AllMethodsFinder {

  private final SourceElement sourceElement;

  @Inject
  AllMethodsFinder(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  /**
   * find all methods in source element, including inherited
   */
  public List<ExecutableElement> findMethodsInSourceElement() {
    TypeMirror mirror = sourceElement.element().asType();
    return findMethodsIn(mirror);
  }

  private List<ExecutableElement> findMethodsIn(TypeMirror mirror) {
    List<ExecutableElement> acc = new ArrayList<>();
    while (true) {
      Optional<TypeElement> element = findMethodsIn(mirror, acc);
      if (element.isEmpty()) {
        return acc;
      }
      mirror = element.get().getSuperclass();
    }
  }

  private Optional<TypeElement> findMethodsIn(
      TypeMirror mirror, List<ExecutableElement> acc) {
    if (mirror.getKind() != TypeKind.DECLARED) {
      return Optional.empty();
    }
    DeclaredType declared = AS_DECLARED.visit(mirror);
    if (declared == null) {
      return Optional.empty();
    }
    TypeElement typeElement = AS_TYPE_ELEMENT.visit(declared.asElement());
    if (typeElement == null) {
      return Optional.empty();
    }
    List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
    for (TypeMirror anInterface : interfaces) {
      findMethodsIn(anInterface, acc);// recursion
    }
    if (!typeElement.getModifiers().contains(ABSTRACT)) {
      // no relevant methods
      return Optional.empty();
    }
    acc.addAll(methodsIn(typeElement.getEnclosedElements()));
    return Optional.of(typeElement);
  }
}
