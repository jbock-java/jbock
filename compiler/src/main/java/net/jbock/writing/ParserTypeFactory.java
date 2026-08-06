package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.simple.Inject;
import net.jbock.parse.StandardParser;
import net.jbock.parse.SuperParser;
import net.jbock.parse.VarargsParameterParser;

import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

final class ParserTypeFactory extends HasCommandRepresentation {

    @Inject
    ParserTypeFactory(
            CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
    }

    private final Supplier<ParserType> parserType = memoize(() -> {
        if (isSuperCommand()) {
            return new ParserType(ClassName.get(SuperParser.class));
        } else if (varargsParameter().isPresent()) {
            return new ParserType(ClassName.get(VarargsParameterParser.class));
        } else {
            return new ParserType(ClassName.get(StandardParser.class));
        }
    });

    ParserType get() {
        return parserType.get();
    }
}
