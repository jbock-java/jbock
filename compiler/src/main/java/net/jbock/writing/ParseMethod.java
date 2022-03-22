package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import javax.inject.Inject;
import net.jbock.util.ExFailure;

import java.util.function.Supplier;

import static io.jbock.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.EITHER;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Suppliers.memoize;

@WritingScope
final class ParseMethod extends HasCommandRepresentation {

    private final GeneratedTypes generatedTypes;
    private final CreateModelMethod createModelMethod;
    private final ExtractMethod extractMethod;
    private final ParserTypeFactory parserTypeFactory;

    @Inject
    ParseMethod(
            GeneratedTypes generatedTypes,
            CommandRepresentation commandRepresentation,
            CreateModelMethod createModelMethod,
            ExtractMethod extractMethod,
            ParserTypeFactory parserTypeFactory) {
        super(commandRepresentation);
        this.generatedTypes = generatedTypes;
        this.createModelMethod = createModelMethod;
        this.extractMethod = extractMethod;
        this.parserTypeFactory = parserTypeFactory;
    }

    private final Supplier<MethodSpec> define = memoize(() -> {

        ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();

        CodeBlock.Builder code = CodeBlock.builder();

        ParserType parserType = parserTypeFactory().get();

        ParameterSpec e = builder(Exception.class, "e").build();
        ParameterSpec parser = parserType.asParam();
        code.addStatement("$T $N = $L", parserType.type(), parser, parserType.init());
        code.add("try {\n").indent()
                .addStatement("$N.parse($N)", parser, tokens)
                .addStatement("return $T.right($N($N))", EITHER,
                        harvestMethod().get(), parser)
                .unindent().add("} catch ($T $N) {\n", ExFailure.class, e).indent()
                .addStatement("return $T.left($N.toError($N()))",
                        EITHER, e, createModelMethod().get())
                .unindent().add("}\n");

        return MethodSpec.methodBuilder("parse")
                .addParameter(tokens)
                .returns(generatedTypes().parseResultType())
                .addCode(code.build())
                .addModifiers(sourceElement().accessModifiers())
                .build();
    });

    MethodSpec get() {
        return define.get();
    }

    private ParserTypeFactory parserTypeFactory() {
        return parserTypeFactory;
    }

    private ExtractMethod harvestMethod() {
        return extractMethod;
    }

    private CreateModelMethod createModelMethod() {
        return createModelMethod;
    }

    private GeneratedTypes generatedTypes() {
        return generatedTypes;
    }
}
