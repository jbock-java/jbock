package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

@ContextScope
public class OptionNamesMethod extends CachedMethod {

    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final SourceElement sourceElement;
    private final CommonFields commonFields;

    @Inject
    OptionNamesMethod(
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
                commonFields.optionNames().type, "result").build();
        CodeBlock code = namedOptions.isEmpty() ?
                CodeBlock.builder().addStatement("return $T.of()", Map.class).build() :
                regularCode(result);
        return MethodSpec.methodBuilder("optionNames")
                .addCode(code)
                .returns(result.type)
                .addModifiers(PRIVATE, STATIC)
                .build();
    }

    private CodeBlock regularCode(ParameterSpec result) {
        long mapSize = namedOptions.stream()
                .map(Mapping::sourceMethod)
                .map(AnnotatedOption::names)
                .map(List::size)
                .mapToLong(i -> i)
                .sum();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = new $T<>($L)", result.type, result, HashMap.class, mapSize);
        for (Mapping<AnnotatedOption> namedOption : namedOptions) {
            String enumConstant = namedOption.enumName().enumConstant();
            for (String dashedName : namedOption.sourceMethod().names()) {
                code.addStatement("$N.put($S, $T.$L)",
                        result, dashedName, sourceElement.optionEnumType(),
                        enumConstant);
            }
        }
        code.addStatement("return $N", result);
        return code.build();
    }
}
