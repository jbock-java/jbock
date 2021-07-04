package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.util.ParseRequest;

import javax.inject.Inject;

import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class ParseMethod extends CachedMethod {

    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final ParseMethodOverloadRequest parseMethodOverloadRequest;

    @Inject
    ParseMethod(
            GeneratedTypes generatedTypes,
            SourceElement sourceElement,
            ParseMethodOverloadRequest parseMethodOverloadRequest) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = sourceElement;
        this.parseMethodOverloadRequest = parseMethodOverloadRequest;
    }

    @Override
    MethodSpec define() {

        ParameterSpec args = builder(STRING_ARRAY, "args").build();
        CodeBlock code = CodeBlock.builder()
                .addStatement("return $N($T.prepare($N).build())",
                        parseMethodOverloadRequest.get(), ParseRequest.class, args)
                .build();

        return MethodSpec.methodBuilder("parse")
                .addParameter(args)
                .varargs(true)
                .returns(generatedTypes.parseResultType())
                .addCode(code)
                .addModifiers(sourceElement.accessModifiers())
                .build();
    }
}
