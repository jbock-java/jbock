package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.ParameterizedTypeName;
import io.jbock.simple.Inject;
import net.jbock.parse.StandardParser;
import net.jbock.parse.SuperParser;
import net.jbock.parse.VarargsParameterParser;

import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

final class ParserTypeFactory extends HasCommandRepresentation {

    private final OptionStatesMethod optionStatesMethod;
    private final OptionNamesMethod optionNamesMethod;

    @Inject
    ParserTypeFactory(
            CommandRepresentation commandRepresentation,
            OptionStatesMethod optionStatesMethod,
            OptionNamesMethod optionNamesMethod) {
        super(commandRepresentation);
        this.optionStatesMethod = optionStatesMethod;
        this.optionNamesMethod = optionNamesMethod;
    }

    private final Supplier<ParserType> parserType = memoize(() -> {
        if (isSuperCommand()) {
            ClassName parserClass = ClassName.get(SuperParser.class);
            return new ParserType(ParameterizedTypeName.get(parserClass, optType()));
        } else if (varargsParameter().isPresent()) {
            ClassName parserClass = ClassName.get(VarargsParameterParser.class);
            return new ParserType(ParameterizedTypeName.get(parserClass, optType()));
        } else {
            ClassName parserClass = ClassName.get(StandardParser.class);
            return new ParserType(ParameterizedTypeName.get(parserClass, optType()));
        }
    });

    ParserType get() {
        return parserType.get();
    }

    private OptionStatesMethod optionStatesMethod() {
        return optionStatesMethod;
    }

    private OptionNamesMethod optionNamesMethod() {
        return optionNamesMethod;
    }
}
