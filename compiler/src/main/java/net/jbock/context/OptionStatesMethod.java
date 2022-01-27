package net.jbock.context;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.parse.OptionState;
import net.jbock.parse.OptionStateModeFlag;
import net.jbock.parse.OptionStateNonRepeatable;
import net.jbock.parse.OptionStateRepeatable;
import net.jbock.processor.SourceElement;

import java.util.EnumMap;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.mapOf;

@ContextScope
public final class OptionStatesMethod extends Cached<MethodSpec> {

    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final SourceElement sourceElement;
    private final CommonFields commonFields;

    @Inject
    OptionStatesMethod(
            List<Mapping<AnnotatedOption>> namedOptions,
            SourceElement sourceElement,
            CommonFields commonFields) {
        this.namedOptions = namedOptions;
        this.sourceElement = sourceElement;
        this.commonFields = commonFields;
    }

    @Override
    MethodSpec define() {
        ParameterSpec result = ParameterSpec.builder(
                mapOf(commonFields.optType(), ClassName.get(OptionState.class)), "result").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = new $T<>($T.class)", result.type, result, EnumMap.class, sourceElement.optionEnumType());
        for (Mapping<AnnotatedOption> namedOption : namedOptions) {
            code.addStatement("$N.put($T.$L, new $T())",
                    result, sourceElement.optionEnumType(),
                    namedOption.enumName(), optionParserType(namedOption));
        }
        code.addStatement("return $N", result);
        return MethodSpec.methodBuilder("optionStates")
                .addCode(code.build())
                .returns(result.type)
                .addModifiers(PRIVATE)
                .build();
    }

    private ClassName optionParserType(Mapping<AnnotatedOption> param) {
        if (param.isRepeatable()) {
            return ClassName.get(OptionStateRepeatable.class);
        }
        if (param.isModeFlag()) {
            return ClassName.get(OptionStateModeFlag.class);
        }
        return ClassName.get(OptionStateNonRepeatable.class);
    }
}
