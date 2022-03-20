package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.HashMap;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

@WritingScope
final class OptionNamesMethod extends Cached<MethodSpec> {

    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final SourceElement sourceElement;
    private final CommonFields commonFields;

    @Inject
    OptionNamesMethod(
            List<Mapping<AnnotatedOption>> namedOptions,
            SourceElement sourceElement,
            CommonFields.Factory commonFieldsFactory) {
        this.namedOptions = namedOptions;
        this.sourceElement = sourceElement;
        this.commonFields = commonFieldsFactory.create(sourceElement, namedOptions);
    }

    @Override
    MethodSpec define() {
        ParameterSpec result = ParameterSpec.builder(
                commonFields.optionNames().type, "result").build();
        long mapSize = namedOptions.stream()
                .map(Mapping::sourceMethod)
                .map(AnnotatedOption::names)
                .map(List::size)
                .mapToLong(i -> i)
                .sum();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = new $T<>($L)", result.type, result, HashMap.class, mapSize);
        for (Mapping<AnnotatedOption> namedOption : namedOptions) {
            for (String dashedName : namedOption.sourceMethod().names()) {
                code.addStatement("$N.put($S, $T.$L)",
                        result, dashedName, sourceElement.optionEnumType(),
                        namedOption.enumName());
            }
        }
        code.addStatement("return $N", result);
        return MethodSpec.methodBuilder("optionNames")
                .addCode(code.build())
                .returns(result.type)
                .addModifiers(PRIVATE, STATIC)
                .build();
    }
}
