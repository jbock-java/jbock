package net.jbock.annotated;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreTypes;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

final class AnnotationUtil {

    private static final String CONVERTER_ATTRIBUTE = "converter";

    private static final Set<String> ANNOTATIONS = Stream.of(
                    Parameter.class,
                    Parameters.class,
                    Option.class)
            .map(Class::getCanonicalName).collect(toSet());

    // visible for testing
    static final AnnotationValueVisitor<Optional<TypeMirror>, Void> GET_TYPE = new SimpleAnnotationValueVisitor9<>() {

        @Override
        public Optional<TypeMirror> visitType(TypeMirror mirror, Void nothing) {
            return Optional.of(mirror);
        }

        @Override
        protected Optional<TypeMirror> defaultAction(Object o, Void nothing) {
            return Optional.empty();
        }
    };

    Optional<TypeElement> getConverterAttribute(ExecutableElement sourceMethod) {
        return getAnnotationMirror(sourceMethod)
                .map(this::getAnnotationValue)
                .flatMap(GET_TYPE::visit)
                .map(MoreTypes::asTypeElement)
                .filter(this::isNotVoid);
    }

    private Optional<AnnotationMirror> getAnnotationMirror(ExecutableElement sourceMethod) {
        return sourceMethod.getAnnotationMirrors().stream()
                .filter(this::hasAnnotationTypeIn)
                .map((AnnotationMirror a) -> a) // Avoid returning Optional<? extends AnnotationMirror>.
                .findFirst();
    }

    private boolean hasAnnotationTypeIn(AnnotationMirror annotation) {
        return ANNOTATIONS.contains(
                MoreTypes.asTypeElement(annotation.getAnnotationType()).getQualifiedName().toString());
    }

    private AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror) {
        return AnnotationMirrors.getAnnotationValue(annotationMirror, CONVERTER_ATTRIBUTE);
    }

    private boolean isNotVoid(TypeElement typeElement) {
        return !"java.lang.Void".equals(typeElement.getQualifiedName().toString());
    }
}
