package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.parse.RestParser;
import net.jbock.parse.RestlessParser;
import net.jbock.parse.SuperParser;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@ContextScope
public final class ParserTypeFactory extends Cached<ParserType> {

    private final SourceElement sourceElement;
    private final List<Mapping<AnnotatedParameter>> positionalParameters;
    private final List<Mapping<AnnotatedParameters>> repeatablePositionalParameters;
    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final CommonFields commonFields;
    private final OptionStatesMethod optionStatesMethod;

    @Inject
    ParserTypeFactory(
            SourceElement sourceElement,
            List<Mapping<AnnotatedParameter>> positionalParameters,
            List<Mapping<AnnotatedParameters>> repeatablePositionalParameters,
            List<Mapping<AnnotatedOption>> namedOptions,
            CommonFields commonFields,
            OptionStatesMethod optionStatesMethod) {
        this.sourceElement = sourceElement;
        this.positionalParameters = positionalParameters;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.namedOptions = namedOptions;
        this.commonFields = commonFields;
        this.optionStatesMethod = optionStatesMethod;
    }

    @Override
    ParserType define() {
        FieldSpec optionNames = commonFields.optionNames();
        MethodSpec optionStates = optionStatesMethod.get();
        CodeBlock optionNamesBlock = namedOptions.isEmpty() ?
                CodeBlock.of("$T.of()", Map.class) :
                CodeBlock.of("$N", optionNames);
        CodeBlock optionStatesBlock = namedOptions.isEmpty() ?
                CodeBlock.of("$T.of()", Map.class) :
                CodeBlock.of("$N()", optionStates);
        int numParams = positionalParameters.size();
        if (sourceElement.isSuperCommand()) {
            ClassName parserClass = ClassName.get(SuperParser.class);
            CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                    ClassName.get(SuperParser.class),
                    optionNamesBlock, optionStatesBlock, numParams);
            return ParserType.create(ParameterizedTypeName.get(parserClass, commonFields.optType()), init);
        }
        if (!repeatablePositionalParameters.isEmpty()) {
            ClassName parserClass = ClassName.get(RestParser.class);
            CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                    ClassName.get(RestParser.class),
                    optionNamesBlock, optionStatesBlock, numParams);
            return ParserType.create(ParameterizedTypeName.get(parserClass, commonFields.optType()), init);
        }
        ClassName parserClass = ClassName.get(RestlessParser.class);
        CodeBlock init = CodeBlock.of("$T.create($L, $L, $L)",
                ClassName.get(RestlessParser.class),
                optionNamesBlock, optionStatesBlock, numParams);
        return ParserType.create(ParameterizedTypeName.get(parserClass, commonFields.optType()), init);
    }
}
