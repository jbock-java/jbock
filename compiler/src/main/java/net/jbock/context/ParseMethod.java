package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.util.AtFileError;
import net.jbock.util.AtFileReader;
import net.jbock.util.ExNotSuccess;
import net.jbock.util.HelpRequested;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.EITHER;
import static net.jbock.common.Constants.STRING_ARRAY;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class ParseMethod extends CachedMethod {

    private final GeneratedTypes generatedTypes;
    private final AllItems allItems;
    private final SourceElement sourceElement;
    private final BuildMethod buildMethod;
    private final CreateModelMethod createModelMethod;

    @Inject
    ParseMethod(
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

        ParameterSpec args = builder(STRING_ARRAY, "args").build();
        ParameterSpec it = builder(STRING_ITERATOR, "it").build();
        ParameterSpec err = builder(AtFileError.class, "err").build();

        CodeBlock.Builder code = CodeBlock.builder();

        if (sourceElement.helpEnabled()) {
            if (allItems.anyRequired()) {
                code.add("if ($1N.length == 0 || $2S.equals($1N[0]))\n", args, "--help").indent()
                        .addStatement("return $T.left(new $T($N()))", EITHER, HelpRequested.class,
                                createModelMethod.get())
                        .unindent();
            } else {
                code.add("if ($1N.length > 0 && $2S.equals($1N[0]))\n", args, "--help").indent()
                        .addStatement("return $T.left(new $T($N()))", EITHER, HelpRequested.class,
                                createModelMethod.get())
                        .unindent();
            }
        }

        if (sourceElement.atFileExpansion()) {
            code.addStatement("return $L", CodeBlock.builder()
                    .add("new $T().read($N)\n", AtFileReader.class, args).indent()
                    .add(".mapLeft($1N -> $1N.addModel($2N()))\n", err, createModelMethod.get())
                    .add(".map($T::iterator)\n", List.class)
                    .add(".flatMap($N -> {\n", it)
                    .indent().add(coreBlock(it)).unindent()
                    .add("})").unindent()
                    .build());
        } else {
            code.addStatement("$T $N = $T.asList($N).iterator()", it.type, it, Arrays.class, args);
            code.add(coreBlock(it));
        }

        return MethodSpec.methodBuilder("parse")
                .addParameter(args)
                .varargs(true)
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
                .add("return $T.left($N.toError($N()));\n",
                        EITHER, e, createModelMethod.get())
                .unindent().add("}\n")
                .build();
    }
}
