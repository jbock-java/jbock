package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import jakarta.inject.Inject;
import net.jbock.contrib.StandardErrorHandler;
import net.jbock.processor.SourceElement;
import net.jbock.util.AtFileError;
import net.jbock.util.ParseRequest;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class ParseOrExitMethod {

    private final SourceElement sourceElement;
    private final GeneratedTypes generatedTypes;
    private final ParseMethod parseMethod;
    private final CreateModelMethod createModelMethod;

    @Inject
    ParseOrExitMethod(
            SourceElement sourceElement,
            GeneratedTypes generatedTypes,
            ParseMethod parseMethod,
            CreateModelMethod createModelMethod) {
        this.sourceElement = sourceElement;
        this.generatedTypes = generatedTypes;
        this.parseMethod = parseMethod;
        this.createModelMethod = createModelMethod;
    }

    MethodSpec define() {

        ParameterSpec args = builder(STRING_ARRAY, "args").build();
        ParameterSpec notSuccess = builder(generatedTypes.parseResultType(), "failure").build();
        ParameterSpec err = builder(AtFileError.class, "err").build();

        CodeBlock.Builder code = CodeBlock.builder();
        code.beginControlFlow("if ($1N.length > 0 && $2S.equals($1N[0]))", args, "--help")
                .add("$T.builder().build()\n", StandardErrorHandler.class).indent()
                .add(".printUsageDocumentation($N());\n", createModelMethod.get()).unindent()
                .addStatement("$T.exit(0)", System.class)
                .endControlFlow();

        code.add("return $T.from($N).expand()\n", ParseRequest.class, args).indent()
                .add(".mapLeft($1N -> $1N.addModel($2N()))\n", err, createModelMethod.get())
                .add(".flatMap(this::$N)\n", parseMethod.get())
                .add(".orElseThrow($N -> {\n", notSuccess).indent()
                .addStatement("$T.builder().build().printErrorMessage($N)",
                        StandardErrorHandler.class, notSuccess)
                .addStatement("$T.exit(1)", System.class)
                .addStatement("return new $T()", RuntimeException.class).unindent()
                .addStatement("})").unindent();
        return methodBuilder("parseOrExit").addParameter(args)
                .addModifiers(sourceElement.accessModifiers())
                .returns(generatedTypes.parseSuccessType())
                .addCode(code.build())
                .build();
    }
}
