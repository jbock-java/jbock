package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

final class ParserType {

    private final TypeName type;
    private final CodeBlock init;

    private final ParameterSpec parser;

    private ParserType(TypeName type, CodeBlock init, ParameterSpec parser) {
        this.type = type;
        this.init = init;
        this.parser = parser;
    }

    static ParserType create(TypeName type, CodeBlock init) {
        return new ParserType(type, init, ParameterSpec.builder(type, "parser").build());
    }

    TypeName type() {
        return type;
    }

    CodeBlock init() {
        return init;
    }

    ParameterSpec asParam() {
        return parser;
    }
}
