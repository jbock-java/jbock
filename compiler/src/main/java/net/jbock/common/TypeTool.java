package net.jbock.common;

import io.jbock.simple.Inject;

import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleAnnotationValueVisitor14;
import javax.lang.model.util.SimpleElementVisitor14;
import javax.lang.model.util.SimpleTypeVisitor14;
import java.util.List;
import java.util.Optional;

@Inject
public record TypeTool(SafeElements elements, SafeTypes types) {

    private abstract static class OptionalTypeVisitor<E> extends SimpleTypeVisitor14<Optional<E>, Void> {

        @Override
        protected final Optional<E> defaultAction(TypeMirror e, Void nothing) {
            return Optional.empty();
        }
    }

    public static final TypeVisitor<Optional<DeclaredType>, Void> AS_DECLARED = new OptionalTypeVisitor<>() {
        @Override
        public Optional<DeclaredType> visitDeclared(DeclaredType declaredType, Void nothing) {
            return Optional.of(declaredType);
        }
    };

    public static final TypeVisitor<Optional<PrimitiveType>, Void> AS_PRIMITIVE = new OptionalTypeVisitor<>() {
        @Override
        public Optional<PrimitiveType> visitPrimitive(PrimitiveType primitiveType, Void nothing) {
            return Optional.of(primitiveType);
        }
    };

    public static final ElementVisitor<Optional<TypeElement>, Void> AS_TYPE_ELEMENT = new SimpleElementVisitor14<>() {
        @Override
        public Optional<TypeElement> visitType(TypeElement typeElement, Void nothing) {
            return Optional.of(typeElement);
        }

        @Override
        protected Optional<TypeElement> defaultAction(Element e, Void nothing) {
            return Optional.empty();
        }
    };

    public static final AnnotationValueVisitor<Optional<TypeMirror>, Void> ANNOTATION_VALUE_AS_TYPE = new SimpleAnnotationValueVisitor14<>() {

        @Override
        public Optional<TypeMirror> visitType(TypeMirror mirror, Void nothing) {
            return Optional.of(mirror);
        }

        @Override
        protected Optional<TypeMirror> defaultAction(Object o, Void nothing) {
            return Optional.empty();
        }
    };

    /**
     * Compares {@code mirror} to the erasure of {@code cl}.
     */
    public boolean isErasureOf(TypeMirror mirror, Class<?> cl) {
        return elements.getTypeElement(cl.getCanonicalName())
                .map(TypeElement::asType)
                .map(types::erasure)
                .map(type -> types.isSameType(mirror, type))
                .orElse(false);
    }

    public boolean isList(TypeMirror mirror) {
        Optional<DeclaredType> visit = AS_DECLARED.visit(mirror);
        if (visit.isEmpty()) {
            return false;
        }
        DeclaredType declaredType = visit.orElseThrow();
        if (declaredType.getTypeArguments().isEmpty()) {
            return false;
        }
        return isErasureOf(types.erasure(declaredType), List.class);
    }

    public boolean isListOfString(TypeMirror mirror) {
        Optional<DeclaredType> visit = AS_DECLARED.visit(mirror);
        if (visit.isEmpty()) {
            return false;
        }
        DeclaredType declaredType = visit.orElseThrow();
        if (declaredType.getTypeArguments().isEmpty()) {
            return false;
        }
        boolean isList = isErasureOf(types.erasure(declaredType), List.class);
        return isList && isErasureOf(declaredType.getTypeArguments().getFirst(), String.class);
    }

    /**
     * {@code someClass} must be a class with exactly one type parameter.
     */
    public Optional<TypeMirror> getSingleTypeArgument(
            TypeMirror mirror, Class<?> clazz) {
        return AS_DECLARED.visit(mirror)
                .filter(declaredType -> declaredType.getTypeArguments().size() == 1)
                .filter(declaredType -> {
                    TypeMirror x = types.erasure(declaredType);
                    return isErasureOf(types.erasure(x), clazz);
                })
                .map(declaredType -> declaredType.getTypeArguments().getFirst());
    }
}
