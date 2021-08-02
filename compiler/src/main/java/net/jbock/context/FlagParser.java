package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.common.Constants;
import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

@ContextScope
public class FlagParser {

    private final GeneratedTypes generatedTypes;
    private final UnixClustering unixClustering;
    private final CommonFields commonFields;
    private final ContextUtil contextUtil;

    @Inject
    FlagParser(
            GeneratedTypes generatedTypes,
            UnixClustering unixClustering,
            CommonFields commonFields,
            ContextUtil contextUtil) {
        this.generatedTypes = generatedTypes;
        this.unixClustering = unixClustering;
        this.commonFields = commonFields;
        this.contextUtil = contextUtil;
    }

    TypeSpec define() {
        return TypeSpec.classBuilder(generatedTypes.flagParserType())
                .superclass(generatedTypes.optionParserType())
                .addField(commonFields.seen())
                .addMethod(readMethodFlag())
                .addMethod(streamMethodFlag())
                .addModifiers(PRIVATE, STATIC).build();
    }

    private MethodSpec readMethodFlag() {
        ParameterSpec token = ParameterSpec.builder(Constants.STRING, "token").build();
        ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
        return MethodSpec.methodBuilder("read")
                .addException(ExToken.class)
                .addCode(unixClustering.unixClusteringSupported() ?
                        readMethodFlagCodeClustering(token) :
                        readMethodFlagCodeSimple(token))
                .returns(unixClustering.readMethodReturnType())
                .addParameters(List.of(token, it)).build();
    }

    private CodeBlock readMethodFlagCodeClustering(ParameterSpec token) {
        CodeBlock.Builder code = CodeBlock.builder();
        code.add("if ($N)\n", commonFields.seen()).indent()
                .addStatement(contextUtil.throwRepetitionErrorStatement(token))
                .unindent();
        code.addStatement("$N = $L", commonFields.seen(), true);
        code.add("if ($1N.startsWith($2S) || $1N.length() == 2)\n", token, "--").indent()
                .addStatement("return null")
                .unindent();
        code.addStatement("return '-' + $N.substring(2)", token);
        return code.build();
    }

    private CodeBlock readMethodFlagCodeSimple(ParameterSpec token) {
        CodeBlock.Builder code = CodeBlock.builder();
        code.add("if (!$1N.startsWith($2S) && $1N.length() > 2)\n", token, "--").indent()
                .addStatement("throw new $T($T.$L, $N)", ExToken.class, ErrTokenType.class,
                        ErrTokenType.INVALID_OPTION, token)
                .unindent();
        code.add("if ($N)\n", commonFields.seen()).indent()
                .addStatement(contextUtil.throwRepetitionErrorStatement(token))
                .unindent();
        code.addStatement("$N = $L", commonFields.seen(), true);
        return code.build();
    }

    private MethodSpec streamMethodFlag() {
        ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
        return MethodSpec.methodBuilder("stream")
                .returns(streamOfString)
                .addStatement("return $N ? $T.of($S) : $T.empty()", commonFields.seen(), Stream.class, "", Stream.class)
                .build();
    }
}
