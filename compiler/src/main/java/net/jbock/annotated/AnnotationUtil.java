package net.jbock.annotated;

import com.google.auto.common.AnnotationMirrors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import java.util.Optional;

import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

final class AnnotationUtil {

    private static final String CONVERTER_ATTRIBUTE = "converter";

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
        AnnotationValue converter = AnnotationMirrors.getAnnotationValue(mirror, CONVERTER_ATTRIBUTE);
        return GET_TYPE.visit(converter)
                .flatMap(AS_DECLARED::visit)
                .map(DeclaredType::asElement)
                .flatMap(AS_TYPE_ELEMENT::visit)
                .filter(element -> !"java.lang.Void".equals(element.getQualifiedName().toString()));
    }
}
