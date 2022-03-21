package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Suppliers.memoize;

@WritingScope
final class OptionNamesMethod {

    private final CommandRepresentation commandRepresentation;

    @Inject
    OptionNamesMethod(
            CommandRepresentation commandRepresentation) {
        this.commandRepresentation = commandRepresentation;
    }

    private final Supplier<MethodSpec> define = memoize(() -> {
        ParameterSpec result = ParameterSpec.builder(
                optionNames().type, "result").build();
        long mapSize = namedOptions().stream()
                .map(Mapping::sourceMethod)
                .map(AnnotatedOption::names)
                .map(List::size)
                .mapToLong(i -> i)
                .sum();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = new $T<>($L)", result.type, result, HashMap.class, mapSize);
        for (Mapping<AnnotatedOption> namedOption : namedOptions()) {
            for (String dashedName : namedOption.sourceMethod().names()) {
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

    private List<Mapping<AnnotatedOption>> namedOptions() {
        return commandRepresentation.namedOptions();
    }

    private FieldSpec optionNames() {
        return commandRepresentation.optionNames();
    }

    private SourceElement sourceElement() {
        return commandRepresentation.sourceElement();
    }
}
