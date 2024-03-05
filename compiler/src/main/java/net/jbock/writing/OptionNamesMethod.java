package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.simple.Inject;
import net.jbock.annotated.Option;
import net.jbock.convert.Mapping;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.mapOf;
import static net.jbock.common.Suppliers.memoize;

final class OptionNamesMethod extends HasCommandRepresentation {

    @Inject
    OptionNamesMethod(
            CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
    }

    private final Supplier<MethodSpec> define = memoize(() -> {
        ParameterSpec result = ParameterSpec.builder(
                mapOf(STRING, optType()), "result").build();
        long mapSize = namedOptions().stream()
                .map(Mapping::item)
                .map(Option::names)
                .map(List::size)
                .mapToLong(i -> i)
                .sum();
        CodeBlock.Builder code = CodeBlock.builder();
        int capacity = (int) (1 + Math.max(mapSize * 1.35, 15));
        code.addStatement("$T $N = new $T<>($L)", result.type, result, HashMap.class, capacity);
        for (Mapping<Option> namedOption : namedOptions()) {
            for (String dashedName : namedOption.item().names()) {
                code.addStatement("$N.put($S, $T.$L)",
                        result, dashedName, sourceElement().optionEnumType(),
                        namedOption.enumName());
            }
        }
        code.addStatement("return $N", result);
        return MethodSpec.methodBuilder("optionNames")
                .addCode(code.build())
                .returns(result.type)
                .addModifiers(PRIVATE, STATIC)
                .build();
    });

    MethodSpec get() {
        return define.get();
    }
}
