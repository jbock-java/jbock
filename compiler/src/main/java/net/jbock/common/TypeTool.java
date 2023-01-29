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
import javax.lang.model.util.SimpleAnnotationValueVisitor9;
import javax.lang.model.util.SimpleElementVisitor9;
import javax.lang.model.util.SimpleTypeVisitor9;
import java.util.Optional;

public final class TypeTool {

    private abstract static class OptionalTypeVisitor<E> extends SimpleTypeVisitor9<Optional<E>, Void> {

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

    public static final ElementVisitor<Optional<TypeElement>, Void> AS_TYPE_ELEMENT = new SimpleElementVisitor9<>() {
        @Override
        public Optional<TypeElement> visitType(TypeElement typeElement, Void nothing) {
            return Optional.of(typeElement);
        }

        @Override
        protected Optional<TypeElement> defaultAction(Element e, Void nothing) {
            return Optional.empty();
        }
    };

    public static final AnnotationValueVisitor<Optional<TypeMirror>, Void> ANNOTATION_VALUE_AS_TYPE = new SimpleAnnotationValueVisitor9<>() {

        @Override
        public Optional<TypeMirror> visitType(TypeMirror mirror, Void nothing) {
            return Optional.of(mirror);
        }

        @Override
        protected Optional<TypeMirror> defaultAction(Object o, Void nothing) {
            return Optional.empty();
        }
    };

    private final SafeTypes types;

    private final SafeElements elements;

    @Inject
    public TypeTool(SafeElements elements, SafeTypes types) {
        this.types = types;
        this.elements = elements;
    }

    /**
     * Works for classes with no type parameters.
     */
    public boolean isSameType(TypeMirror mirror, Class<?> cl) {
        return isSameType(mirror, cl.getCanonicalName());
    }

    /**
     * Works for classes with no type parameters.
     */
    public boolean isSameType(TypeMirror mirror, String canonicalName) {
        return elements.getTypeElement(canonicalName)
                .map(TypeElement::asType)
                .map(type -> types.isSameType(mirror, type))
                .orElse(false);
    }

    /**
     * {@code someClass} must be a class with exactly one type parameter.
     */
    public Optional<TypeMirror> getSingleTypeArgument(
            TypeMirror mirror, TypeElement someClass) {
        return AS_DECLARED.visit(mirror)
                .filter(declaredType -> declaredType.getTypeArguments().size() == 1)
                .filter(declaredType -> types.isSameType(
                        types.erasure(declaredType),
                        types.erasure(someClass.asType())))
                .map(declaredType -> declaredType.getTypeArguments().get(0));
    }

    public SafeTypes types() {
        return types;
    }

    public SafeElements elements() {
        return elements;
    }
}
