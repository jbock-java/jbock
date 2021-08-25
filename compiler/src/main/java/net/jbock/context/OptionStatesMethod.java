package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.state.OptionState;
import net.jbock.state.OptionStateModeFlag;
import net.jbock.state.OptionStateRegular;
import net.jbock.state.OptionStateRepeatable;

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

    @Inject
    OptionStatesMethod(
            List<Mapping<AnnotatedOption>> namedOptions,
            SourceElement sourceElement) {
        this.namedOptions = namedOptions;
        this.sourceElement = sourceElement;
    }

    @Override
    MethodSpec define() {
        ParameterSpec result = ParameterSpec.builder(mapOf(
                namedOptions.isEmpty() ?
                        ClassName.get(Void.class) :
                        sourceElement.optionEnumType(),
                ClassName.get(OptionState.class)),
                "result").build();
        CodeBlock.Builder code = CodeBlock.builder();
        if (namedOptions.isEmpty()) {
            code.addStatement("$T $N = $T.of()", result.type, result, Map.class);
        } else {
            code.addStatement("$T $N = new $T<>($T.class)", result.type, result, EnumMap.class, sourceElement.optionEnumType());
        }
        for (Mapping<AnnotatedOption> namedOption : namedOptions) {
            String enumConstant = namedOption.enumName().enumConstant();
            code.addStatement("$N.put($T.$L, new $T())",
                    result, sourceElement.optionEnumType(),
                    enumConstant, optionParserType(namedOption));
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
        if (param.modeFlag()) {
            return ClassName.get(OptionStateModeFlag.class);
        }
        return ClassName.get(OptionStateRegular.class);
    }
}
