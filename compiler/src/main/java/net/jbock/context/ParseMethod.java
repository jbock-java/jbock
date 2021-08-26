package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.parse.Parser;
import net.jbock.parse.RegularParser;
import net.jbock.parse.RepeatableParser;
import net.jbock.parse.SuperParser;
import net.jbock.processor.SourceElement;
import net.jbock.util.ExFailure;

import javax.inject.Inject;
import java.util.List;

import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.EITHER;
import static net.jbock.common.Constants.LIST_OF_STRING;

@ContextScope
public class ParseMethod extends CachedMethod {

    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final CreateModelMethod createModelMethod;
    private final List<Mapping<AnnotatedParameters>> repeatablePositionalParameters;
    private final List<Mapping<AnnotatedParameter>> positionalParameters;
    private final CommonFields commonFields;
    private final HarvestMethod harvestMethod;
    private final OptionStatesMethod optionStatesMethod;

    @Inject
    ParseMethod(
            GeneratedTypes generatedTypes,
            SourceElement sourceElement,
            CreateModelMethod createModelMethod,
            List<Mapping<AnnotatedParameters>> repeatablePositionalParameters,
            List<Mapping<AnnotatedParameter>> positionalParameters,
            CommonFields commonFields,
            HarvestMethod harvestMethod,
            OptionStatesMethod optionStatesMethod) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = sourceElement;
        this.createModelMethod = createModelMethod;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.positionalParameters = positionalParameters;
        this.commonFields = commonFields;
        this.harvestMethod = harvestMethod;
        this.optionStatesMethod = optionStatesMethod;
    }

    @Override
    MethodSpec define() {

        ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();

        CodeBlock.Builder code = CodeBlock.builder();

        ParameterSpec parser = builder(ParameterizedTypeName.get(ClassName.get(Parser.class),
                commonFields.optType()), "parser").build();
        ParameterSpec e = builder(Exception.class, "e").build();
        code.addStatement("$T $N = $L", parser.type, parser, parserInit());
        code.add("try {\n").indent()
                .addStatement("$N.parse($N)", parser, tokens)
                .addStatement("return $T.right($N($N))", EITHER,
                        harvestMethod.get(), parser)
                .unindent().add("} catch ($T $N) {\n", ExFailure.class, e).indent()
                .addStatement("return $T.left($N.toError($N()))",
                        EITHER, e, createModelMethod.get())
                .unindent().add("}\n");

        return MethodSpec.methodBuilder("parse")
                .addParameter(tokens)
                .returns(generatedTypes.parseResultType())
                .addCode(code.build())
                .addModifiers(sourceElement.accessModifiers())
                .build();
    }

    private CodeBlock parserInit() {
        FieldSpec optionNames = commonFields.optionNames();
        MethodSpec optionStates = optionStatesMethod.get();
        int numParams = positionalParameters.size();
        if (sourceElement.isSuperCommand()) {
            return CodeBlock.of("$T.create($N, $N(), $L)",
                    ClassName.get(SuperParser.class),
                    optionNames, optionStates, numParams);
        }
        if (!repeatablePositionalParameters.isEmpty()) {
            return CodeBlock.of("$T.create($N, $N(), $L)",
                    ClassName.get(RepeatableParser.class),
                    optionNames, optionStates, numParams);
        }
        return CodeBlock.of("$T.create($N, $N(), $L)",
                ClassName.get(RegularParser.class),
                optionNames, optionStates, numParams);
    }
}
