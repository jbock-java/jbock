package net.jbock.common;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;
import java.util.Optional;

/**
 * A wrapper around {@link Types} where none of the methods can return {@code null}.
 */
public final class SafeTypes {

    private final Types types;

    public SafeTypes(Types types) {
        this.types = types;
    }

    public Optional<Element> asElement(TypeMirror t) {
        return Optional.ofNullable(types.asElement(t));
    }

    public PrimitiveType getPrimitiveType(TypeKind kind) {
        return types.getPrimitiveType(kind);
    }

    public TypeElement boxedClass(PrimitiveType p) {
        return types.boxedClass(p);
    }

    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return types.isSameType(t1, t2);
    }

    public TypeMirror erasure(TypeMirror t) {
        return types.erasure(t);
    }

    public WildcardType getWildcardType() {
        return types.getWildcardType(null, null);
    }
}
