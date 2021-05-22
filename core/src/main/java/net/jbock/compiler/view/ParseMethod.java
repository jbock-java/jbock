package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.Arrays;

import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

@Reusable
public class ParseMethod extends Cached<MethodSpec> {

  private final GeneratedTypes generatedTypes;
  private final AllParameters allParameters;
  private final SourceElement sourceElement;

  @Inject
  ParseMethod(
      GeneratedTypes generatedTypes,
      AllParameters allParameters,
      SourceElement sourceElement) {
    this.generatedTypes = generatedTypes;
    this.allParameters = allParameters;
    this.sourceElement = sourceElement;
  }

  @Override
  MethodSpec define() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec e = builder(RuntimeException.class, "e").build();
    CodeBlock.Builder code = CodeBlock.builder();

    generatedTypes.helpRequestedType().ifPresent(helpRequestedType -> {
      if (allParameters.anyRequired()) {
        code.add("if ($N.length == 0)\n",
            args).indent()
            .addStatement("return new $T()", helpRequestedType)
            .unindent();
      }
      code.add("if ($1N.length == 1 && ($2S.equals($1N[0]) || $3S.equals($1N[0])))\n",
          args, "--help", "-h").indent()
          .addStatement("return new $T()", helpRequestedType)
          .unindent();
    });

    ParameterSpec state = builder(generatedTypes.statefulParserType(), "statefulParser").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec result = builder(generatedTypes.parseSuccessType(), "result").build();
    code.addStatement("$T $N = new $T()", state.type, state, state.type);
    code.addStatement("$T $N = $T.asList($N).iterator()", it.type, it, Arrays.class, args);
    code.beginControlFlow("try")
        .addStatement("$T $N = $N.parse($N).build()", result.type, result, state, it)
        .addStatement("return new $T($N)", generatedTypes.parsingSuccessWrapperType(), result)
        .endControlFlow();

    code.beginControlFlow("catch ($T $N)", RuntimeException.class, e)
        .addStatement("return new $T($N)",
            generatedTypes.parsingFailedType(), e)
        .endControlFlow();

    return MethodSpec.methodBuilder("parse").addParameter(args)
        .returns(generatedTypes.parseResultType())
        .addCode(code.build())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }
}
