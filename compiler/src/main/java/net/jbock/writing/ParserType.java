package net.jbock.writing;

import io.jbock.javapoet.TypeName;

final class ParserType {

    private final TypeName type;

    ParserType(TypeName type) {
        this.type = type;
    }

    TypeName type() {
        return type;
    }
}
