package net.jbock.convert.reference;

import javax.lang.model.type.TypeMirror;

public class StringConverterType {

    private final TypeMirror typeArgument;
    private final boolean supplier; // wrapped in Supplier?

    StringConverterType(TypeMirror typeArgument, boolean supplier) {
        this.typeArgument = typeArgument;
        this.supplier = supplier;
    }

    public boolean isSupplier() {
        return supplier;
    }

    public TypeMirror outputType() {
        return typeArgument;
    }
}
