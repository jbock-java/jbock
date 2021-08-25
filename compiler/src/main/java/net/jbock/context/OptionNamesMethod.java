package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.mapOf;

@ContextScope
public class OptionNamesMethod extends CachedMethod {

    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final SourceElement sourceElement;

    @Inject
    OptionNamesMethod(
            List<Mapping<AnnotatedOption>> namedOptions,
            SourceElement sourceElement) {
        this.namedOptions = namedOptions;
        this.sourceElement = sourceElement;
    }

    @Override
    MethodSpec define() {
        long mapSize = namedOptions.stream()
                .map(Mapping::sourceMethod)
                .map(AnnotatedOption::names)
                .map(List::size)
                .mapToLong(i -> i)
                .sum();
        ParameterSpec result = ParameterSpec.builder(mapOf(STRING,
                namedOptions.isEmpty() ?
                        ClassName.get(Void.class) :
                        sourceElement.optionEnumType()),
                "result").build();
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
        return MethodSpec.methodBuilder("optionNames")
                .addCode(code.build())
                .returns(result.type)
                .addModifiers(PRIVATE)
                .build();
    }
}
