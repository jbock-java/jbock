package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.util.ExToken;
import net.jbock.util.ErrTokenType;

import javax.inject.Inject;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class StatefulParseMethod {

  private final GeneratedTypes generatedTypes;

  private final ParameterSpec it = builder(STRING_ITERATOR, "it").build();
  private final ParameterSpec token = builder(STRING, "token").build();
  private final ParameterSpec position = builder(INT, "position").build();
  private final ParameterSpec endOfOptionParsing = builder(BOOLEAN, "endOfOptionParsing").build();
  private final NamedOptions options;
  private final PositionalParameters positionalParameters;
  private final CommonFields commonFields;
  private final SourceElement sourceElement;
  private final TryParseOptionMethod tryParseOptionMethod;

  @Inject
  StatefulParseMethod(
      GeneratedTypes generatedTypes,
      NamedOptions options,
      PositionalParameters positionalParameters,
      CommonFields commonFields,
      SourceElement sourceElement,
      TryParseOptionMethod tryParseOptionMethod) {
    this.generatedTypes = generatedTypes;
    this.options = options;
    this.positionalParameters = positionalParameters;
    this.commonFields = commonFields;
    this.sourceElement = sourceElement;
    this.tryParseOptionMethod = tryParseOptionMethod;
  }

  MethodSpec define() {

    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse");
    if (sourceElement.isSuperCommand()) {
      spec.addCode(superCommandCode());
    } else {
      spec.addCode(regularCode());
    }
    return spec.addParameter(it)
        .addException(ExToken.class)
        .returns(generatedTypes.statefulParserType())
        .build();
  }

  private CodeBlock superCommandCode() {
    CodeBlock.Builder code = initVariables();

    // begin parsing loop
    code.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it);

    code.beginControlFlow("if ($N == $L)", position, positionalParameters.size())
        .addStatement("$N.add($N)", commonFields.rest(), token)
        .addStatement("continue")
        .endControlFlow();

    if (!options.isEmpty()) {
      code.add(optionBlock());
    }
    code.add(errorUnrecognizedOption());

    code.addStatement("$N[$N++] = $N", commonFields.params(),
        position, token);

    // end parsing loop
    code.endControlFlow();

    code.addStatement("return this");
    return code.build();
  }

  private CodeBlock regularCode() {
    CodeBlock.Builder code = initVariables();

    // begin parsing loop
    code.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it);

    code.beginControlFlow("if (!$N && $S.equals($N))",
        endOfOptionParsing, "--", token)
        .addStatement("$N = $L", endOfOptionParsing, true)
        .addStatement("continue")
        .endControlFlow();

    if (!options.isEmpty()) {
      code.add(optionBlock());
    }
    code.beginControlFlow("if (!$N)", endOfOptionParsing)
        .add(errorUnrecognizedOption())
        .endControlFlow();

    if (positionalParameters.isEmpty()) {
      code.addStatement(throwInvalidOptionStatement(ErrTokenType.EXCESS_PARAM));
    } else if (!positionalParameters.anyRepeatable()) {
      code.add("if ($N == $L)\n", position, positionalParameters.size()).indent()
          .addStatement(throwInvalidOptionStatement(ErrTokenType.EXCESS_PARAM))
          .unindent();
    }

    if (!positionalParameters.isEmpty()) {
      if (positionalParameters.anyRepeatable()) {
        if (positionalParameters.size() == 1) {
          code.addStatement("$N.add($N)", commonFields.rest(), token);
        } else {
          code.add("if ($N < $L)\n", position, positionalParameters.size() - 1).indent()
              .addStatement("$N[$N++] = $N", commonFields.params(),
                  position, token)
              .unindent()
              .add("else\n").indent()
              .addStatement("$N.add($N)", commonFields.rest(), token)
              .unindent();
        }
      } else {
        code.addStatement("$N[$N++] = $N", commonFields.params(),
            position, token);
      }
    }

    // end parsing loop
    code.endControlFlow();

    return code.addStatement("return this").build();
  }

  private CodeBlock optionBlock() {
    if (sourceElement.isSuperCommand()) {
      return CodeBlock.builder()
          .beginControlFlow("if ($N($N, $N))", tryParseOptionMethod.get(), token, it)
          .addStatement("continue")
          .endControlFlow()
          .build();
    } else {
      return CodeBlock.builder()
          .beginControlFlow("if (!$N && $N($N, $N))", endOfOptionParsing,
              tryParseOptionMethod.get(), token, it)
          .addStatement("continue")
          .endControlFlow()
          .build();
    }
  }

  private CodeBlock.Builder initVariables() {
    CodeBlock.Builder code = CodeBlock.builder();
    if (!positionalParameters.isEmpty() && !(positionalParameters.anyRepeatable() && positionalParameters.size() == 1)) {
      code.addStatement("$T $N = $L", position.type, position, 0);
    }
    if (!sourceElement.isSuperCommand()) {
      code.addStatement("$T $N = $L", BOOLEAN, endOfOptionParsing, false);
    }
    return code;
  }

  CodeBlock errorUnrecognizedOption() {
    return CodeBlock.builder().add("if ($N.matcher($N).matches())\n",
        commonFields.suspiciousPattern(), token).indent()
        .addStatement(throwInvalidOptionStatement(ErrTokenType.INVALID_OPTION))
        .unindent().build();
  }

  private CodeBlock throwInvalidOptionStatement(ErrTokenType errorType) {
    return CodeBlock.of("throw new $T($T.$L, $N)",
        ExToken.class, ErrTokenType.class, errorType, token);
  }
}
