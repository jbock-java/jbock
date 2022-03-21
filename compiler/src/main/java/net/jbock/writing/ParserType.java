package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;

import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

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

    private final Supplier<ParameterSpec> asParamSupplier = memoize(() ->
            ParameterSpec.builder(type(), "parser").build());

    ParameterSpec asParam() {
        return asParamSupplier.get();
    }
}
