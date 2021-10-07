package net.jbock.annotated;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.Map;
import java.util.Optional;

import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

final class AnnotationUtil {

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

    Optional<TypeElement> getConverterAttribute(AnnotationMirror mirror) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                mirror.getElementValues();
        return elementValues.entrySet().stream()
                .filter(e -> "converter".equals(e.getKey().getSimpleName().toString()))
                .map(Map.Entry::getValue)
                .findFirst()
                .flatMap(GET_TYPE::visit)
                .flatMap(AS_DECLARED::visit)
                .map(DeclaredType::asElement)
                .flatMap(AS_TYPE_ELEMENT::visit)
                .filter(element -> !"java.lang.Void".equals(element.getQualifiedName().toString()));
    }
}
