package net.jbock.convert.reference;

import javax.lang.model.type.TypeMirror;

public class StringConverterType {

    private final TypeMirror outputType; // what the converter returns
    private final boolean supplier; // true if Supplier<StringConverter>

    StringConverterType(TypeMirror outputType, boolean supplier) {
        this.outputType = outputType;
        this.supplier = supplier;
    }

    public boolean isSupplier() {
        return supplier;
    }

    public TypeMirror outputType() {
        return outputType;
    }
}
