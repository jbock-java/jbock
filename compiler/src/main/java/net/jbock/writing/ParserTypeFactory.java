package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.ParameterizedTypeName;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.parse.RestParser;
import net.jbock.parse.RestlessParser;
import net.jbock.parse.SuperParser;
import net.jbock.processor.SourceElement;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

@WritingScope
final class ParserTypeFactory {

    private final CommandRepresentation commandRepresentation;
    private final OptionStatesMethod optionStatesMethod;

    @Inject
    ParserTypeFactory(
            CommandRepresentation commandRepresentation,
            OptionStatesMethod optionStatesMethod) {
        this.commandRepresentation = commandRepresentation;
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
        if (sourceElement().isSuperCommand()) {
            ClassName parserClass = ClassName.get(SuperParser.class);
            CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                    ClassName.get(SuperParser.class),
                    optionNames, optionStates, numParams);
            return new ParserType(ParameterizedTypeName.get(parserClass, optType()), init);
        }
        if (!repeatablePositionalParameters().isEmpty()) {
            ClassName parserClass = ClassName.get(RestParser.class);
            CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                    ClassName.get(RestParser.class),
                    optionNames, optionStates, numParams);
            return new ParserType(ParameterizedTypeName.get(parserClass, optType()), init);
        }
        ClassName parserClass = ClassName.get(RestlessParser.class);
        CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                ClassName.get(RestlessParser.class),
                optionNames, optionStates, numParams);
        return new ParserType(ParameterizedTypeName.get(parserClass, optType()), init);
    });

    ParserType get() {
        return define.get();
    }

    private OptionStatesMethod optionStatesMethod() {
        return optionStatesMethod;
    }

    private SourceElement sourceElement() {
        return commandRepresentation.sourceElement();
    }

    private List<Mapping<AnnotatedParameter>> positionalParameters() {
        return commandRepresentation.positionalParameters();
    }

    private List<Mapping<AnnotatedParameters>> repeatablePositionalParameters() {
        return commandRepresentation.repeatablePositionalParameters();
    }

    private List<Mapping<AnnotatedOption>> namedOptions() {
        return commandRepresentation.namedOptions();
    }

    private FieldSpec optionNames() {
        return commandRepresentation.optionNames();
    }

    private ClassName optType() {
        return commandRepresentation.optType();
    }
}
