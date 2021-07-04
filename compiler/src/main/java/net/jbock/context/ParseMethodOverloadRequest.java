package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.util.AtFileError;
import net.jbock.util.AtFileReader;
import net.jbock.util.ExNotSuccess;
import net.jbock.util.HelpRequested;
import net.jbock.util.ParseRequest;

import javax.inject.Inject;
import java.util.List;

import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.EITHER;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class ParseMethodOverloadRequest extends CachedMethod {

    private final GeneratedTypes generatedTypes;
    private final AllItems allItems;
    private final SourceElement sourceElement;
    private final BuildMethod buildMethod;
    private final CreateModelMethod createModelMethod;

    private final ParameterSpec request = builder(ParseRequest.class, "request").build();

    @Inject
    ParseMethodOverloadRequest(
            GeneratedTypes generatedTypes,
            AllItems allItems,
            SourceElement sourceElement,
            BuildMethod buildMethod,
            CreateModelMethod createModelMethod) {
        this.generatedTypes = generatedTypes;
        this.allItems = allItems;
        this.sourceElement = sourceElement;
        this.buildMethod = buildMethod;
        this.createModelMethod = createModelMethod;
    }

    @Override
    MethodSpec define() {

        ParameterSpec it = builder(STRING_ITERATOR, "it").build();
        ParameterSpec err = builder(AtFileError.class, "err").build();

        CodeBlock.Builder code = CodeBlock.builder();

        if (allItems.anyRequired()) {
            code.add("if ($1N.isHelpEnabled() && $1N.isEmpty() || $1N.isHelpRequested())\n",
                    request).indent()
                    .addStatement("return $T.left(new $T($N($N)))", EITHER, HelpRequested.class,
                            createModelMethod.get(), request)
                    .unindent();
        } else {
            code.add("if ($N.isHelpRequested())\n", request).indent()
                    .addStatement("return $T.left(new $T($N($N)))", EITHER, HelpRequested.class,
                            createModelMethod.get(), request)
                    .unindent();
        }

        code.addStatement("return $L", CodeBlock.builder()
                .add("new $T().read($N)\n", AtFileReader.class, request).indent()
                .add(".mapLeft($1N -> $1N.addModel($2N($3N)))\n", err, createModelMethod.get(), request)
                .add(".map($T::iterator)\n", List.class)
                .add(".flatMap($N -> {\n", it)
                .indent().add(coreBlock(it)).unindent()
                .add("})").unindent()
                .build());

        return MethodSpec.methodBuilder("parse")
                .addParameter(request)
                .returns(generatedTypes.parseResultType())
                .addCode(code.build())
                .addModifiers(sourceElement.accessModifiers())
                .build();
    }

    private CodeBlock coreBlock(ParameterSpec it) {
        ParameterSpec state = builder(generatedTypes.statefulParserType(), "statefulParser").build();
        ParameterSpec e = builder(Exception.class, "e").build();
        return CodeBlock.builder().add("$T $N = new $T();\n", state.type, state, state.type)
                .add("try {\n").indent()
                .add("return $T.right($N.parse($N).$N());\n", EITHER, state, it, buildMethod.get())
                .unindent().add("} catch ($T $N) {\n", ExNotSuccess.class, e).indent()
                .add("return $T.left($N.toError($N($N)));\n",
                        EITHER, e, createModelMethod.get(), request)
                .unindent().add("}\n")
                .build();
    }
}
