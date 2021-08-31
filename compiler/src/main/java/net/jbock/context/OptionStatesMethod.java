package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.parse.OptionState;
import net.jbock.parse.OptionStateModeFlag;
import net.jbock.parse.OptionStateNonRepeatable;
import net.jbock.parse.OptionStateRepeatable;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.mapOf;

@ContextScope
public class OptionStatesMethod extends CachedMethod {

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
        CodeBlock code = namedOptions.isEmpty() ?
                CodeBlock.builder().addStatement("return $T.of()", Map.class).build() :
                regularCode(result);
        return MethodSpec.methodBuilder("optionStates")
                .addCode(code)
                .returns(result.type)
                .addModifiers(PRIVATE)
                .build();
    }

    private CodeBlock regularCode(ParameterSpec result) {
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = new $T<>($T.class)", result.type, result, EnumMap.class, sourceElement.optionEnumType());
        for (Mapping<AnnotatedOption> namedOption : namedOptions) {
            String enumConstant = namedOption.enumName().enumConstant();
            code.addStatement("$N.put($T.$L, new $T())",
                    result, sourceElement.optionEnumType(),
                    enumConstant, optionParserType(namedOption));
        }
        code.addStatement("return $N", result);
        return code.build();
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
