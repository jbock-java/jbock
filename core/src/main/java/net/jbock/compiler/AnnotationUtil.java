package net.jbock.compiler;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreTypes;
import net.jbock.Option;
import net.jbock.Param;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

class AnnotationUtil {

  private static final String MAPPER_ATTRIBUTE = "mappedBy";

  private static final Set<String> ANNOTATIONS = Stream.of(Param.class, Option.class)
      .map(Class::getCanonicalName).collect(toSet());

  private static final AnnotationValueVisitor<TypeMirror, Void> GET_TYPE = new SimpleAnnotationValueVisitor8<TypeMirror, Void>() {

    @Override
    public TypeMirror visitType(TypeMirror mirror, Void _null) {
      return mirror;
    }
  };

  Optional<TypeElement> getMapper(ExecutableElement sourceMethod) {
    return getAnnotationMirror(sourceMethod)
        .map(AnnotationUtil::getAnnotationValue)
        .map(AnnotationUtil::asType)
        .map(MoreTypes::asTypeElement)
        .filter(AnnotationUtil::isNotVoid);
  }

  private static Optional<AnnotationMirror> getAnnotationMirror(ExecutableElement sourceMethod) {
    return sourceMethod.getAnnotationMirrors().stream()
        .filter(AnnotationUtil::hasAnnotationTypeIn)
        .map((AnnotationMirror a) -> a) // Avoid returning Optional<? extends AnnotationMirror>.
        .findFirst();
  }

  private static boolean hasAnnotationTypeIn(AnnotationMirror annotation) {
    return ANNOTATIONS.contains(
        MoreTypes.asTypeElement(annotation.getAnnotationType()).getQualifiedName().toString());
  }

  private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror) {
    return AnnotationMirrors.getAnnotationValue(annotationMirror, MAPPER_ATTRIBUTE);
  }

  private static TypeMirror asType(AnnotationValue annotationValue) {
    return GET_TYPE.visit(annotationValue);
  }

  private static boolean isNotVoid(TypeElement typeElement) {
    return !Void.class.getCanonicalName().equals(typeElement.getQualifiedName().toString());
  }
}
