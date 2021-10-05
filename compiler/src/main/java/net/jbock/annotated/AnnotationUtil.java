package net.jbock.annotated;

import com.google.auto.common.AnnotationMirrors;
import net.jbock.common.Annotations;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

final class AnnotationUtil {

    private static final String CONVERTER_ATTRIBUTE = "converter";

    private final Set<String> methodLevelAnnotations = Annotations.methodLevelAnnotations().stream()
            .map(Class::getCanonicalName)
            .collect(toSet());

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
        return getMethodAnnotation(sourceMethod)
                .map(mirror -> AnnotationMirrors.getAnnotationValue(mirror, CONVERTER_ATTRIBUTE))
                .flatMap(GET_TYPE::visit)
                .flatMap(AS_DECLARED::visit)
                .map(DeclaredType::asElement)
                .flatMap(AS_TYPE_ELEMENT::visit)
                .filter(element -> !"java.lang.Void".equals(element.getQualifiedName().toString()));
    }

    private Optional<AnnotationMirror> getMethodAnnotation(ExecutableElement sourceMethod) {
        return sourceMethod.getAnnotationMirrors().stream()
                .filter(mirror -> AS_TYPE_ELEMENT.visit(mirror.getAnnotationType().asElement())
                        .map(TypeElement::getQualifiedName)
                        .map(Name::toString)
                        .filter(methodLevelAnnotations::contains)
                        .isPresent())
                .map((AnnotationMirror a) -> a) // Avoid returning Optional<? extends AnnotationMirror>.
                .findFirst();
    }
}
