package net.jbock.convert;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreTypes;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.either.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

class AnnotationUtil {

  private static final String CONVERTER_ATTRIBUTE = "converter";

  private static final Set<String> ANNOTATIONS = Stream.of(
      Parameter.class,
      Parameters.class,
      Option.class)
      .map(Class::getCanonicalName).collect(toSet());

  private static final AnnotationValueVisitor<Optional<TypeMirror>, Void> GET_TYPE = new SimpleAnnotationValueVisitor9<>() {

    @Override
    public Optional<TypeMirror> visitType(TypeMirror mirror, Void nothing) {
      return Optional.of(mirror);
    }

    @Override
    protected Optional<TypeMirror> defaultAction(Object o, Void nothing) {
      return Optional.empty();
    }
  };

  Optional<TypeElement> getConverter(ExecutableElement sourceMethod) {
    return getAnnotationMirror(sourceMethod)
        .map(AnnotationUtil::getAnnotationValue)
        .flatMap(GET_TYPE::visit)
        .map(MoreTypes::asTypeElement)
        .filter(AnnotationUtil::isNotVoid);
  }

  private static Optional<AnnotationMirror> getAnnotationMirror(ExecutableElement sourceMethod) {
    return sourceMethod.getAnnotationMirrors().stream()
        .filter(AnnotationUtil::hasAnnotationTypeIn)
        .map((AnnotationMirror a) -> a) // Avoid returning Optional<? extends AnnotationMirror>.
        .findFirst()
        .map(Optional::of)
        .orElse(Optional.empty());
  }

  private static boolean hasAnnotationTypeIn(AnnotationMirror annotation) {
    return ANNOTATIONS.contains(
        MoreTypes.asTypeElement(annotation.getAnnotationType()).getQualifiedName().toString());
  }

  private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror) {
    return AnnotationMirrors.getAnnotationValue(annotationMirror, CONVERTER_ATTRIBUTE);
  }

  private static boolean isNotVoid(TypeElement typeElement) {
    return !Void.class.getCanonicalName().equals(typeElement.getQualifiedName().toString());
  }
}
