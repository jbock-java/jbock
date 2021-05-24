package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

@Reusable
public class PrintOptionMethod {

  private final AllParameters allParameters;
  private final SourceElement sourceElement;
  private final CommonFields commonFields;
  private final MakeLinesMethod makeLinesMethod;

  @Inject
  PrintOptionMethod(
      AllParameters allParameters,
      SourceElement sourceElement,
      CommonFields commonFields,
      MakeLinesMethod makeLinesMethod) {
    this.allParameters = allParameters;
    this.sourceElement = sourceElement;
    this.commonFields = commonFields;
    this.makeLinesMethod = makeLinesMethod;
  }

  MethodSpec get() {
    ParameterSpec descriptionKey = builder(STRING, "descriptionKey").build();
    ParameterSpec message = builder(STRING, "message").build();
    ParameterSpec option = builder(sourceElement.optionType(), "option").build();
    ParameterSpec names = builder(STRING, "names").build();
    ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec continuationIndent = builder(STRING, "continuationIndent").build();
    ParameterSpec s = builder(STRING, "s").build();
    CodeBlock.Builder code = CodeBlock.builder();
    if (allParameters.anyDescriptionKeys()) {
      code.addStatement("$T $N = $N.isEmpty() ? null : $N.get($N)",
          message.type, message, descriptionKey, commonFields.messages(), descriptionKey);
    }

    code.addStatement("$T $N = new $T<>()", tokens.type, tokens, ArrayList.class);
    code.addStatement("$N.add($N)", tokens, names);
    if (allParameters.anyDescriptionKeys()) {
      code.addStatement(CodeBlock.builder().add("$N.addAll($T.ofNullable($N)\n",
          tokens, Optional.class, message).indent()
          .add(".map($T::trim)\n", STRING)
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".map($T::asList)\n", Arrays.class)
          .add(".orElseGet(() -> $T.stream($N.description)\n", Arrays.class, option).indent()
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".flatMap($T::stream)\n", Arrays.class)
          .add(".collect($T.toList())))", Collectors.class)
          .unindent()
          .unindent()
          .build());
    } else {
      code.addStatement(CodeBlock.builder()
          .add("$T.stream($N.description)\n", Arrays.class, option).indent()
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".flatMap($T::stream)\n", Arrays.class)
          .add(".forEach($N::add)", tokens)
          .unindent()
          .build());
    }
    code.addStatement("$T $N = $T.join($S, $T.nCopies($N.length() + 1, $S))",
        STRING, continuationIndent, STRING, "", Collections.class, names, " ");
    code.addStatement("$N($N, $N).forEach($N::println)", makeLinesMethod.get(),
        continuationIndent, tokens, commonFields.err());
    MethodSpec.Builder spec = methodBuilder("printOption")
        .addParameter(option)
        .addParameter(names)
        .addModifiers(PRIVATE)
        .addCode(code.build());
    if (allParameters.anyDescriptionKeys()) {
      spec.addParameter(descriptionKey);
    }
    return spec.build();
  }
}
