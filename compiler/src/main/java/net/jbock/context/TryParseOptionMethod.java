package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import javax.inject.Inject;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

public class TryParseOptionMethod extends CachedMethod {

    private final SourceElement sourceElement;
    private final UnixClustering unixClustering;
    private final CommonFields commonFields;
    private final ReadOptionNameMethod readOptionNameMethod;

    @Inject
    TryParseOptionMethod(
            SourceElement sourceElement,
            UnixClustering unixClustering,
            CommonFields commonFields,
            ReadOptionNameMethod readOptionNameMethod) {
        this.sourceElement = sourceElement;
        this.unixClustering = unixClustering;
        this.commonFields = commonFields;
        this.readOptionNameMethod = readOptionNameMethod;
    }

    @Override
    MethodSpec define() {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
        return MethodSpec.methodBuilder("tryParseOption")
                .addException(ExToken.class)
                .addParameter(token)
                .addParameter(it)
                .addCode(unixClustering.unixClusteringSupported() ?
                        tryParseOptionCodeClustering(token, it) :
                        tryParseOptionCodeSimple(token, it))
                .returns(BOOLEAN)
                .build();
    }

    private CodeBlock tryParseOptionCodeClustering(ParameterSpec token, ParameterSpec it) {
        ParameterSpec t = ParameterSpec.builder(STRING, "t").build();
        ParameterSpec option = ParameterSpec.builder(sourceElement.optionEnumType(), "opt").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = $N.get($N($N))", sourceElement.optionEnumType(),
                option, commonFields.optionNames(), readOptionNameMethod.get(), token);
        code.add("if ($N == null)\n", option).indent()
                .addStatement("return false")
                .unindent();
        code.addStatement("$T $N = $N", t.type, t, token);
        code.add("while (($1N = $2N.get($3N).read($1N, $4N)) != null)\n",
                t, commonFields.optionParsers(), option, it).indent();
        code.add("if (($N = $N.get($N($N))) == null)\n", option, commonFields.optionNames(),
                readOptionNameMethod.get(), t).indent();
        code.addStatement("throw new $T($T.$L, $N)", ExToken.class, ErrTokenType.class,
                ErrTokenType.INVALID_UNIX_GROUP, token);
        code.unindent().unindent();
        code.addStatement("return true");
        return code.build();
    }

    private CodeBlock tryParseOptionCodeSimple(ParameterSpec token, ParameterSpec it) {
        ParameterSpec option = ParameterSpec.builder(sourceElement.optionEnumType(), "option").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = $N.get($N($N))", sourceElement.optionEnumType(), option,
                commonFields.optionNames(), readOptionNameMethod.get(), token);
        code.add("if ($N == null)\n", option).indent()
                .addStatement("return false")
                .unindent();
        code.addStatement("$N.get($N).read($N, $N)", commonFields.optionParsers(), option, token, it)
                .addStatement("return true");
        return code.build();
    }
}
