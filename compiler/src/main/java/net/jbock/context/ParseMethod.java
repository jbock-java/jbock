package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.util.ExNotSuccess;

import javax.inject.Inject;

import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.EITHER;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class ParseMethod extends CachedMethod {

    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final BuildMethod buildMethod;
    private final CreateModelMethod createModelMethod;

    @Inject
    ParseMethod(
            GeneratedTypes generatedTypes,
            SourceElement sourceElement,
            BuildMethod buildMethod,
            CreateModelMethod createModelMethod) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = sourceElement;
        this.buildMethod = buildMethod;
        this.createModelMethod = createModelMethod;
    }

    @Override
    MethodSpec define() {

        ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
        ParameterSpec it = builder(STRING_ITERATOR, "it").build();

        CodeBlock.Builder code = CodeBlock.builder();

        code.addStatement("$T $N = $N.iterator()", STRING_ITERATOR, it, tokens);
        ParameterSpec state = builder(generatedTypes.statefulParserType(), "statefulParser").build();
        ParameterSpec e = builder(Exception.class, "e").build();
        code.add("$T $N = new $T();\n", state.type, state, state.type)
                .add("try {\n").indent()
                .add("return $T.right($N.parse($N).$N());\n", EITHER, state, it, buildMethod.get())
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
}
