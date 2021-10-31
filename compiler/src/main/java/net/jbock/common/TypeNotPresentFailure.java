package net.jbock.common;

public class TypeNotPresentFailure {

    private final String typeName;

    private TypeNotPresentFailure(String typeName) {
        this.typeName = typeName;
    }

    public static TypeNotPresentFailure create(String typeName) {
        return new TypeNotPresentFailure(typeName);
    }
}
