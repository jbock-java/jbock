package net.jbock.writing;

import io.jbock.javapoet.ArrayTypeName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.simple.Inject;
import net.jbock.contrib.StandardErrorHandler;
import net.jbock.util.AtFileError;
import net.jbock.util.ParseRequest;

import javax.lang.model.element.Modifier;
import java.util.List;

import static io.jbock.javapoet.MethodSpec.methodBuilder;
import static io.jbock.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;

final class ParseOrExitMethod extends HasCommandRepresentation {

    private final GeneratedTypes generatedTypes;
    private final ParseMethod parseMethod;
    private final CreateModelMethod createModelMethod;

    @Inject
    ParseOrExitMethod(
            CommandRepresentation commandRepresentation,
            GeneratedTypes generatedTypes,
            ParseMethod parseMethod,
            CreateModelMethod createModelMethod) {
        super(commandRepresentation);
        this.generatedTypes = generatedTypes;
        this.parseMethod = parseMethod;
        this.createModelMethod = createModelMethod;
    }

    MethodSpec define() {

        ParameterSpec args = parseOrExitMethodAcceptsList() ?
                builder(LIST_OF_STRING, "args").build() :
                builder(ArrayTypeName.of(STRING), "args").build();
        ParameterSpec notSuccess = builder(generatedTypes.parseResultType(), "failure").build();
        ParameterSpec err = builder(AtFileError.class, "err").build();

        CodeBlock.Builder code = CodeBlock.builder();
        if (parseOrExitMethodAcceptsList()) {
            code.beginControlFlow("if (!$1N.isEmpty() && $2S.equals($1N.get(0)))", args, "--help");
        } else {
            code.beginControlFlow("if ($1N.length > 0 && $2S.equals($1N[0]))", args, "--help");
        }
        code.add("$T.builder().build()\n", StandardErrorHandler.class).indent()
                .add(".printUsageDocumentation($N());\n", createModelMethod.get()).unindent()
                .addStatement("$T.exit(0)", System.class)
                .endControlFlow();

        if (enableAtFileExpansion()) {
            code.add("return $T.from($N).expand()\n", ParseRequest.class, args).indent()
                    .add(".mapLeft($1N -> $1N.addModel($2N()))\n", err, createModelMethod.get())
                    .add(".flatMap(this::$N)\n", parseMethod.get())
                    .add(".orElseThrow($N -> {\n", notSuccess).indent()
                    .addStatement("$T.builder().build().printErrorMessage($N)",
                            StandardErrorHandler.class, notSuccess)
                    .addStatement("$T.exit(1)", System.class)
                    .addStatement("return new $T()", RuntimeException.class).unindent()
                    .addStatement("})").unindent();
        } else {
            CodeBlock pArgs = parseOrExitMethodAcceptsList() ?
                    CodeBlock.of("$N", args) :
                    CodeBlock.of("$T.of($N)", List.class, args);
            code.add("return parse($L).orElseThrow($N -> {\n", pArgs, notSuccess).indent()
                    .addStatement("$T.builder().build().printErrorMessage($N)",
                            StandardErrorHandler.class, notSuccess)
                    .addStatement("$T.exit(1)", System.class)
                    .addStatement("return new $T()", RuntimeException.class).unindent()
                    .addStatement("})");
        }
        return methodBuilder("parseOrExit").addParameter(args)
                .addModifiers(sourceElement().accessModifiers())
                .returns(generatedTypes.sourceElement().typeName())
                .addModifiers(Modifier.STATIC)
                .addCode(code.build())
                .build();
    }
}
