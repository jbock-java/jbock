package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.contrib.StandardErrorHandler;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.util.ParseRequest;

import javax.inject.Inject;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.INT;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class ParseOrExitMethod {

    private final SourceElement sourceElement;
    private final GeneratedTypes generatedTypes;
    private final ParseMethod parseMethod;
    private final List<Mapping<?>> everything;

    @Inject
    ParseOrExitMethod(
            SourceElement sourceElement,
            GeneratedTypes generatedTypes,
            ParseMethod parseMethod,
            List<Mapping<?>> everything) {
        this.sourceElement = sourceElement;
        this.generatedTypes = generatedTypes;
        this.parseMethod = parseMethod;
        this.everything = everything;
    }

    MethodSpec define() {

        ParameterSpec args = builder(STRING_ARRAY, "args").build();
        ParameterSpec notSuccess = builder(generatedTypes.parseResultType(), "notSuccess").build();
        ParameterSpec returnCode = builder(INT, "code").build();
        ParameterSpec request = builder(ParseRequest.class, "request").build();

        CodeBlock.Builder code = CodeBlock.builder();
        code.add("$1T $2N = $1T.standardBuilder($3N)\n", ParseRequest.class, request, args).indent();
        if (everything.stream().anyMatch(Mapping::isRequired)) {
            code.add(".withHelpRequested($1N.length == 0 || $2S.equals($1N[0]))\n", args, "--help");
        } else {
            code.add(".withHelpRequested($1N.length > 0 && $2S.equals($1N[0]))\n", args, "--help");
        }
        code.addStatement(".build()").unindent();
        code.add("return $N($N)", parseMethod.get(), request)
                .add(".orElseThrow($N -> {\n", notSuccess).indent()
                .addStatement("$T $N = $T.builder().build().handle($N)", INT, returnCode,
                        StandardErrorHandler.class, notSuccess)
                .addStatement("$T.exit($N)", System.class, returnCode)
                .addStatement("return new $T()", RuntimeException.class).unindent()
                .addStatement("})");
        return methodBuilder("parseOrExit").addParameter(args)
                .addModifiers(sourceElement.accessModifiers())
                .returns(generatedTypes.parseSuccessType())
                .addCode(code.build())
                .build();
    }
}
