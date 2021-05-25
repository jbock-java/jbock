package net.jbock.convert;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Util {

  public <E> List<E> concat(List<? extends E> list1, List<? extends E> list2) {
    List<E> result = new ArrayList<>(list1.size() + list2.size());
    result.addAll(list1);
    result.addAll(list2);
    return result;
  }

  public Optional<String> commonTypeChecks(TypeElement classToCheck) {
    if (classToCheck.getNestingKind().isNested() && !classToCheck.getModifiers().contains(Modifier.STATIC)) {
      return Optional.of("must be static or top-level");
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

  public List<TypeElement> getEnclosingElements(TypeElement sourceElement) {
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

  public static String typeToString(TypeMirror type) {
    if (type.getKind() != TypeKind.DECLARED) {
      return type.toString();
    }
    DeclaredType declared = TypeTool.AS_DECLARED.visit(type);
    String base = TypeTool.AS_TYPE_ELEMENT.visit(declared.asElement()).getSimpleName().toString();
    if (declared.getTypeArguments().isEmpty()) {
      return base;
    }
    return base + declared.getTypeArguments().stream().map(Util::typeToString)
        .collect(Collectors.joining(", ", "<", ">"));
  }

  public Optional<String> assertAtLeastOneAnnotation(
      Element element,
      Class<? extends Annotation> ann1,
      Class<? extends Annotation> ann2,
      Class<? extends Annotation> ann3) {
    return assertAtLeastOneAnnotation(element, List.of(ann1, ann2, ann3));
  }

  private Optional<String> assertAtLeastOneAnnotation(
      Element element,
      List<Class<? extends Annotation>> annotations) {
    for (Class<? extends Annotation> annotation : annotations) {
      if (element.getAnnotation(annotation) != null) {
        return Optional.empty();
      }
    }
    return Optional.of("add one of these annotations: " + annotations.stream()
        .map(ann -> "@" + ann.getSimpleName())
        .collect(Collectors.joining(", ")));
  }

  public Optional<String> assertNoDuplicateAnnotations(
      Element element,
      Class<? extends Annotation> ann1,
      Class<? extends Annotation> ann2,
      Class<? extends Annotation> ann3) {
    return assertNoDuplicateAnnotations(element, List.of(ann1, ann2, ann3));
  }

  private Optional<String> assertNoDuplicateAnnotations(
      Element element,
      List<Class<? extends Annotation>> annotations) {
    Class<?> found = null;
    for (Class<? extends Annotation> annotation : annotations) {
      if (element.getAnnotation(annotation) != null) {
        if (found != null) {
          return Optional.of("annotate with either @" + found.getSimpleName() +
              " or @" + annotation.getSimpleName() + " but not both");
        }
        found = annotation;
      }
    }
    return Optional.empty();
  }
}
