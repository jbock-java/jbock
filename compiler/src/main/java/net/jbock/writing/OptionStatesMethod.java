package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.simple.Inject;
import net.jbock.annotated.Option;
import net.jbock.convert.Mapping;
import net.jbock.parse.OptionState;
import net.jbock.parse.OptionStateModeFlag;
import net.jbock.parse.OptionStateNonRepeatable;
import net.jbock.parse.OptionStateRepeatable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.mapOf;
import static net.jbock.common.Suppliers.memoize;

final class OptionStatesMethod extends HasCommandRepresentation {

    @Inject
    OptionStatesMethod(CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
    }

    private final Supplier<MethodSpec> define = memoize(() -> {
        ParameterSpec optionStates = ParameterSpec.builder(
                mapOf(optType(), ClassName.get(OptionState.class)), "optionStates").build();
        CodeBlock.Builder code = CodeBlock.builder();
        if (namedOptions().isEmpty()) {
            code.addStatement("$T $N = $T.of()", optionStates.type, optionStates, Map.class);
        } else {
            code.addStatement("$T $N = new $T<>($T.class)", optionStates.type, optionStates, EnumMap.class, sourceElement().optionEnumType());
        }
        for (Mapping<Option> namedOption : namedOptions()) {
            code.addStatement("$N.put($T.$L, new $T())",
                    optionStates, sourceElement().optionEnumType(),
                    namedOption.enumName(), optionParserType(namedOption));
        }
        code.addStatement("return $N", optionStates);
        return MethodSpec.methodBuilder("optionStates")
                .addCode(code.build())
                .returns(optionStates.type)
                .addModifiers(PRIVATE, STATIC)
                .build();
    });

    MethodSpec get() {
        return define.get();
    }

    private ClassName optionParserType(Mapping<Option> param) {
        if (param.isRepeatable()) {
            return ClassName.get(OptionStateRepeatable.class);
        }
        if (param.isNullary()) {
            return ClassName.get(OptionStateModeFlag.class);
        }
        return ClassName.get(OptionStateNonRepeatable.class);
    }
}
