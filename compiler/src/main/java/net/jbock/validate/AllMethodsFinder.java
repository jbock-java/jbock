package net.jbock.validate;

import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.lang.model.element.ElementKind.INTERFACE;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.util.ElementFilter.methodsIn;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class AllMethodsFinder {

  private final SourceElement sourceElement;

  @Inject
  AllMethodsFinder(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  /**
   * find all methods in source element, including inherited
   */
  List<ExecutableElement> findMethodsInSourceElement() {
    TypeMirror mirror = sourceElement.element().asType();
    return findMethodsIn(mirror);
  }

  private List<ExecutableElement> findMethodsIn(TypeMirror mirror) {
    Map<Name, List<ExecutableElement>> methodsInInterfaces = findMethodsInInterfaces(mirror);
    List<ExecutableElement> acc = new ArrayList<>();
    methodsInInterfaces.values().forEach(acc::addAll);
    while (true) {
      Optional<TypeElement> element = asAbstractTypeElement(mirror);
      if (element.isEmpty()) {
        return acc;
      }
      TypeElement el = element.get();
      List<ExecutableElement> methods = methodsIn(el.getEnclosedElements());
      acc.addAll(methods);
      mirror = el.getSuperclass();
    }
  }

  private Optional<TypeElement> asAbstractTypeElement(TypeMirror mirror) {
    return AS_DECLARED.visit(mirror)
        .map(DeclaredType::asElement)
        .flatMap(AS_TYPE_ELEMENT::visit)
        // interfaces are handled separately
        .filter(typeElement -> typeElement.getKind() != INTERFACE)
        // not abstract -> no relevant methods
        .filter(typeElement -> typeElement.getModifiers().contains(ABSTRACT));
  }

  private Map<Name, List<ExecutableElement>> findMethodsInInterfaces(TypeMirror mirror) {
    return AS_DECLARED.visit(mirror)
        .map(DeclaredType::asElement)
        .flatMap(AS_TYPE_ELEMENT::visit)
        .map(typeElement -> {
          List<ExecutableElement> methods = typeElement.getKind() == INTERFACE ?
              methodsIn(typeElement.getEnclosedElements()) :
              List.of();
          Map<Name, List<ExecutableElement>> acc = new HashMap<>();
          acc.put(typeElement.getQualifiedName(), methods);
          for (TypeMirror superInterface : typeElement.getInterfaces()) {
            acc.putAll(findMethodsInInterfaces(superInterface)); // recursion
          }
          return acc;
        }).orElse(Map.of());
  }
}
