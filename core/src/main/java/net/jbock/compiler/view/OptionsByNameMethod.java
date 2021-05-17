package net.jbock.compiler.view;


import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.mapOf;

@Reusable
public class OptionsByNameMethod {

  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;

  @Inject
  OptionsByNameMethod(SourceElement sourceElement, NamedOptions namedOptions) {
    this.sourceElement = sourceElement;
    this.namedOptions = namedOptions;
  }

  MethodSpec define() {
    ParameterSpec result = builder(mapOf(STRING, sourceElement.optionType()), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();
    long mapSize = namedOptions.stream()
        .map(ConvertedParameter::parameter)
        .map(NamedOption::names)
        .map(List::size)
        .mapToLong(i -> i)
        .sum();
    code.addStatement("$T $N = new $T<>($L)", result.type, result, HashMap.class, mapSize);
    for (ConvertedParameter<NamedOption> namedOption : namedOptions.options()) {
      for (String dashedName : namedOption.parameter().names()) {
        code.addStatement("$N.put($S, $T.$L)", result, dashedName, sourceElement.optionType(),
            namedOption.enumConstant());
      }
    }
    code.addStatement("return $N", result);

    return MethodSpec.methodBuilder("optionsByName").returns(result.type)
        .addCode(code.build())
        .addModifiers(PRIVATE, STATIC)
        .build();
  }
}
