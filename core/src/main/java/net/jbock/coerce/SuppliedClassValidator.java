package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SuppliedClassValidator {

  public static Optional<String> commonChecks(TypeElement classToCheck) {
    if (classToCheck.getNestingKind().isNested() && !classToCheck.getModifiers().contains(Modifier.STATIC)) {
      return Optional.of("must be static or top-level");
    }
    if (classToCheck.getKind() == ElementKind.INTERFACE) {
      return Optional.of("cannot be an interface");
    }
    for (TypeElement element : getEnclosingElements(classToCheck)) {
      if (element.getModifiers().contains(Modifier.PRIVATE)) {
        return Optional.of("class cannot be private");
      }
    }
    if (!hasDefaultConstructor(classToCheck)) {
      return Optional.of("missing default constructor");
    }
    return Optional.empty();
  }

  public static List<TypeElement> getEnclosingElements(TypeElement sourceElement) {
    List<TypeElement> result = new ArrayList<>();
    TypeElement current = sourceElement;
    result.add(current);
    while (current.getNestingKind() == NestingKind.MEMBER) {
      Element enclosingElement = current.getEnclosingElement();
      if (enclosingElement.getKind() != ElementKind.CLASS) {
        return result;
      }
      current = TypeTool.AS_TYPE_ELEMENT.visit(enclosingElement);
      result.add(current);
    }
    return result;
  }

  private static boolean hasDefaultConstructor(TypeElement classToCheck) {
    List<ExecutableElement> constructors = ElementFilter.constructorsIn(classToCheck.getEnclosedElements());
    if (constructors.isEmpty()) {
      return true;
    }
    for (ExecutableElement constructor : constructors) {
      if (!constructor.getParameters().isEmpty()) {
        continue;
      }
      if (constructor.getModifiers().contains(Modifier.PRIVATE)) {
        return false;
      }
      return constructor.getThrownTypes().isEmpty();
    }
    return false;
  }
}
