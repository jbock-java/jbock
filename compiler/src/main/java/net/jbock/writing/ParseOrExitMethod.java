package net.jbock.writing;

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import io.jbock.simple.Inject;
import net.jbock.ColorMode;
import net.jbock.contrib.StandardErrorHandler;
import net.jbock.util.AtFileError;
import net.jbock.util.ParseRequest;

import javax.lang.model.element.Modifier;
import java.util.List;

import static com.palantir.javapoet.MethodSpec.methodBuilder;
import static com.palantir.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;

@Inject
record ParseOrExitMethod(
        CommandRepresentation cr,
        GeneratedTypes generatedTypes,
        ParseMethod parseMethod,
        CreateModelMethod createModelMethod) {

    MethodSpec define() {

        ParameterSpec args = cr.parseOrExitMethodAcceptsList() ?
                builder(LIST_OF_STRING, "args").build() :
                builder(ArrayTypeName.of(STRING), "args").build();
        ParameterSpec notSuccess = builder(generatedTypes.parseResultType(), "failure").build();
        ParameterSpec err = builder(AtFileError.class, "err").build();

        CodeBlock.Builder code = CodeBlock.builder();
        if (!cr.sourceElement().skipHelp()) {
            if (cr.parseOrExitMethodAcceptsList()) {
                code.beginControlFlow("if (!$1N.isEmpty() && $2S.equals($1N.get(0)))", args, "--help");
            } else {
                code.beginControlFlow("if ($1N.length > 0 && $2S.equals($1N[0]))", args, "--help");
            }
            code.add("$L\n", createErrorHandler()).indent()
                    .add(".printUsageDocumentation($N());\n", createModelMethod.get()).unindent()
                    .addStatement("$T.exit(0)", System.class)
                    .endControlFlow();
        }
        if (cr.enableAtFileExpansion()) {
            code.add("return $T.from($N).expand()\n", ParseRequest.class, args).indent()
                    .add(".mapLeft($1N -> $1N.addModel($2N()))\n", err, createModelMethod.get())
                    .add(".flatMap(this::$N)\n", parseMethod.get())
                    .add(".orElseThrow($N -> {\n", notSuccess).indent()
                    .add("$L\n", createErrorHandler()).indent()
                    .add(".printErrorMessage($N);\n", notSuccess).unindent()
                    .addStatement("$T.exit(1)", System.class)
                    .addStatement("return new $T()", RuntimeException.class).unindent()
                    .addStatement("})").unindent();
        } else {
            CodeBlock pArgs = cr.parseOrExitMethodAcceptsList() ?
                    CodeBlock.of("$N", args) :
                    CodeBlock.of("$T.of($N)", List.class, args);
            code.add("return parse($L).orElseThrow($N -> {\n", pArgs, notSuccess).indent()
                    .add("$L\n", createErrorHandler()).indent()
                    .add(".printErrorMessage($N);\n", notSuccess).unindent()
                    .addStatement("$T.exit(1)", System.class)
                    .addStatement("return new $T()", RuntimeException.class).unindent()
                    .addStatement("})");
        }
        return methodBuilder("parseOrExit").addParameter(args)
                .addModifiers(cr.sourceElement().accessModifiers())
                .returns(cr.sourceElement().typeName())
                .addModifiers(Modifier.STATIC)
                .addCode(code.build())
                .build();
    }

    private CodeBlock createErrorHandler() {
        if (cr().sourceElement().colorMode() == ColorMode.AUTO) {
            return CodeBlock.of("$T.builder().build()", StandardErrorHandler.class);
        } else {
            return CodeBlock.of("$T.builder().withColorMode($T.$L).build()",
                    StandardErrorHandler.class, ColorMode.class, cr().sourceElement().colorMode().name());
        }
    }
}
