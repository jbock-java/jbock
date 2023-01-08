package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.ParameterizedTypeName;
import net.jbock.parse.StandardParser;
import net.jbock.parse.SuperParser;
import net.jbock.parse.VarargsParameterParser;

import java.util.Map;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

final class ParserTypeFactory extends HasCommandRepresentation {

    private final OptionStatesMethod optionStatesMethod;

    ParserTypeFactory(
            CommandRepresentation commandRepresentation,
            OptionStatesMethod optionStatesMethod) {
        super(commandRepresentation);
        this.optionStatesMethod = optionStatesMethod;
    }

    private final Supplier<ParserType> define = memoize(() -> {
        CodeBlock optionNames = namedOptions().isEmpty() ?
                CodeBlock.of("$T.of()", Map.class) :
                CodeBlock.of("$N", optionNames());
        CodeBlock optionStates = namedOptions().isEmpty() ?
                CodeBlock.of("$T.of()", Map.class) :
                CodeBlock.of("$N()", optionStatesMethod().get());
        int numParams = positionalParameters().size();
        if (isSuperCommand()) {
            ClassName parserClass = ClassName.get(SuperParser.class);
            CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                    ClassName.get(SuperParser.class),
                    optionNames, optionStates, numParams);
            return new ParserType(ParameterizedTypeName.get(parserClass, optType()), init);
        }
        if (varargsParameter().isPresent()) {
            ClassName parserClass = ClassName.get(VarargsParameterParser.class);
            CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                    ClassName.get(VarargsParameterParser.class),
                    optionNames, optionStates, numParams);
            return new ParserType(ParameterizedTypeName.get(parserClass, optType()), init);
        }
        ClassName parserClass = ClassName.get(StandardParser.class);
        CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                ClassName.get(StandardParser.class),
                optionNames, optionStates, numParams);
        return new ParserType(ParameterizedTypeName.get(parserClass, optType()), init);
    });

    ParserType get() {
        return define.get();
    }

    private OptionStatesMethod optionStatesMethod() {
        return optionStatesMethod;
    }
}
