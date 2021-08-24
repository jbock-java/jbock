package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.state.OptionState;
import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import javax.inject.Inject;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class TryReadOptionMethod extends CachedMethod {

    private final SourceElement sourceElement;
    private final CommonFields commonFields;

    @Inject
    TryReadOptionMethod(
            SourceElement sourceElement,
            CommonFields commonFields) {
        this.sourceElement = sourceElement;
        this.commonFields = commonFields;
    }

    @Override
    MethodSpec define() {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
        return MethodSpec.methodBuilder("tryReadOption")
                .addException(ExToken.class)
                .addParameter(token)
                .addParameter(it)
                .addCode(tryReadOptionCode(token, it))
                .returns(BOOLEAN)
                .build();
    }

    private CodeBlock tryReadOptionCode(ParameterSpec token, ParameterSpec it) {
        ParameterSpec t = ParameterSpec.builder(STRING, "t").build();
        ParameterSpec option = ParameterSpec.builder(sourceElement.optionEnumType(), "opt").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = $N.get($T.readOptionName($N))", sourceElement.optionEnumType(),
                option, commonFields.optionNames(), OptionState.class, token);
        code.add("if ($N == null)\n", option).indent()
                .addStatement("return false")
                .unindent();
        code.addStatement("$T $N = $N", t.type, t, token);
        code.add("while (($1N = $2N.get($3N).read($1N, $4N)) != null)\n",
                t, commonFields.optionParsers(), option, it).indent();
        code.add("if (($N = $N.get($T.readOptionName($N))) == null)\n", option, commonFields.optionNames(),
                OptionState.class, t).indent();
        code.addStatement("throw new $T($T.$L, $N)", ExToken.class, ErrTokenType.class,
                ErrTokenType.INVALID_UNIX_GROUP, token);
        code.unindent().unindent();
        code.addStatement("return true");
        return code.build();
    }
}
