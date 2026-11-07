package net.jbock.convert.match;

import net.jbock.annotated.Item;
import net.jbock.model.Multiplicity;

import javax.lang.model.type.TypeMirror;

public record Match<M extends Item>(Multiplicity multiplicity, M item) {

    public static <M extends Item> Match<M> create(
            Multiplicity multiplicity,
            M item) {
        return new Match<>(multiplicity, item);
    }

    public TypeMirror baseType() {
        return item.baseType();
    }
}
