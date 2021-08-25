package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.state.GenericParser;
import net.jbock.state.RegularParser;
import net.jbock.state.RepeatableParser;
import net.jbock.state.SuperParser;
import net.jbock.util.ExNotSuccess;

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
    private final ConstructMethod constructMethod;
    private final OptionStatesMethod optionStatesMethod;

    @Inject
    ParseMethod(
            GeneratedTypes generatedTypes,
            SourceElement sourceElement,
            CreateModelMethod createModelMethod,
            List<Mapping<AnnotatedParameters>> repeatablePositionalParameters,
            List<Mapping<AnnotatedParameter>> positionalParameters,
            CommonFields commonFields,
            ConstructMethod constructMethod,
            OptionStatesMethod optionStatesMethod) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = sourceElement;
        this.createModelMethod = createModelMethod;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.positionalParameters = positionalParameters;
        this.commonFields = commonFields;
        this.constructMethod = constructMethod;
        this.optionStatesMethod = optionStatesMethod;
    }

    @Override
    MethodSpec define() {

        ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();

        CodeBlock.Builder code = CodeBlock.builder();

        ParameterSpec state = builder(ParameterizedTypeName.get(ClassName.get(GenericParser.class), commonFields.optType()), "statefulParser").build();
        ParameterSpec e = builder(Exception.class, "e").build();
        code.addStatement("$T $N = new $T<>($N, $N(), $L)", state.type, state, parserClass(),
                commonFields.optionNames(), optionStatesMethod.get(), positionalParameters.size());
        code.add("try {\n").indent()
                .add("return $T.right($N($N.parse($N)));\n", EITHER,
                        constructMethod.get(), state, tokens)
                .unindent().add("} catch ($T $N) {\n", ExNotSuccess.class, e).indent()
                .add("return $T.left($N.toError($N()));\n",
                        EITHER, e, createModelMethod.get())
                .unindent().add("}\n");

        return MethodSpec.methodBuilder("parse")
                .addParameter(tokens)
                .returns(generatedTypes.parseResultType())
                .addCode(code.build())
                .addModifiers(sourceElement.accessModifiers())
                .build();
    }

    private ClassName parserClass() {
        if (sourceElement.isSuperCommand()) {
            return ClassName.get(SuperParser.class);
        }
        if (repeatablePositionalParameters.isEmpty()) {
            return ClassName.get(RegularParser.class);
        }
        return ClassName.get(RepeatableParser.class);
    }
}
