package net.jbock.coerce;

import net.jbock.compiler.TypeTool;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Util {

  public static String addBreaks(String code) {
    return code.replace(" ", "$W");
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

  public static Optional<String> assertAtLeastOneAnnotation(
      Element element,
      Class<? extends Annotation> ann1,
      Class<? extends Annotation> ann2,
      Class<? extends Annotation> ann3) {
    return assertAtLeastOneAnnotation(element, Arrays.asList(ann1, ann2, ann3));
  }

  private static Optional<String> assertAtLeastOneAnnotation(
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

  public static Optional<String> assertNoDuplicateAnnotations(
      Element element,
      Class<? extends Annotation> ann1,
      Class<? extends Annotation> ann2) {
    return assertNoDuplicateAnnotations(element, Arrays.asList(ann1, ann2));
  }

  public static Optional<String> assertNoDuplicateAnnotations(
      Element element,
      Class<? extends Annotation> ann1,
      Class<? extends Annotation> ann2,
      Class<? extends Annotation> ann3) {
    return assertNoDuplicateAnnotations(element, Arrays.asList(ann1, ann2, ann3));
  }

  private static Optional<String> assertNoDuplicateAnnotations(
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
