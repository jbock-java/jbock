package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.TypeName;

final class ParserType {

    private final TypeName type;
    private final CodeBlock init;

    ParserType(TypeName type, CodeBlock init) {
        this.type = type;
        this.init = init;
    }

    TypeName type() {
        return type;
    }

    CodeBlock init() {
        return init;
    }
}
