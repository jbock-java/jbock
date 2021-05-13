package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;

import javax.inject.Inject;
import java.util.ArrayList;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

class StatefulParseMethod {

  private final GeneratedTypes generatedTypes;

  private final ParameterSpec it = builder(STRING_ITERATOR, "it").build();
  private final ParameterSpec token = builder(STRING, "token").build();
  private final ParameterSpec position = builder(INT, "position").build();
  private final FieldSpec rest = FieldSpec.builder(LIST_OF_STRING, "rest").build();
  private final NamedOptions options;
  private final PositionalParameters positionalParameters;
  private final CommonFields commonFields;

  @Inject
  StatefulParseMethod(
      GeneratedTypes generatedTypes,
      NamedOptions options,
      PositionalParameters positionalParameters,
      CommonFields commonFields) {
    this.generatedTypes = generatedTypes;
    this.options = options;
    this.positionalParameters = positionalParameters;
    this.commonFields = commonFields;
  }

  MethodSpec parseMethod() {

    return MethodSpec.methodBuilder("parse")
        .addParameter(it)
        .addCode(generatedTypes.parseResultWithRestType()
            .map(this::superCommandCode)
            .orElseGet(this::regularCode))
        .returns(generatedTypes.parseSuccessType())
        .build();
  }

  private CodeBlock superCommandCode(ClassName parseResultWithRestType) {
    CodeBlock.Builder code = initVariables();
    code.addStatement("$T $N = new $T<>()", rest.type, rest, ArrayList.class);

    // begin parsing loop
    code.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it);

    code.beginControlFlow("if ($N == $L)", position, positionalParameters.size())
        .addStatement("$N.add($N)", rest, token)
        .addStatement("continue")
        .endControlFlow();

    if (!options.isEmpty()) {
      code.add(optionBlock());
    }
    code.add(errorUnrecognizedOption());

    code.addStatement("paramParsers[$N] = $N", position, token)
        .addStatement("$N++", position);

    // end parsing loop
    code.endControlFlow();

    code.addStatement("return new $T(build(), $N.toArray(new $T[0]))",
        parseResultWithRestType, rest, String.class);
    return code.build();
  }

  private CodeBlock regularCode() {
    CodeBlock.Builder code = initVariables();
    FieldSpec endOfOptionParsing = FieldSpec.builder(BOOLEAN, "endOfOptionParsing").build();

    // begin parsing loop
    code.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it);

    code.beginControlFlow("if (!$N && $S.equals($N))", endOfOptionParsing, "--", token)
        .addStatement("$N = $L", endOfOptionParsing, true)
        .addStatement("continue")
        .endControlFlow();

    if (!options.isEmpty()) {
      code.add(optionBlock());
    }
    code.beginControlFlow("if (!$N)", endOfOptionParsing)
        .add(errorUnrecognizedOption())
        .endControlFlow();

    if (positionalParameters.none()) {
      code.addStatement(throwInvalidOptionStatement("Excess param"));
    } else if (!positionalParameters.anyRepeatable()) {
      code.add("if ($N == $L)\n", position, positionalParameters.size()).indent()
          .addStatement(throwInvalidOptionStatement("Excess param"))
          .unindent();
    }

    if (!positionalParameters.none()) {
      if (positionalParameters.anyRepeatable()) {
        if (positionalParameters.size() == 1) {
          code.addStatement("$N.add($N)", rest, token);
        } else {
          code.add("if ($N < $L)\n", position, positionalParameters.size() - 1).indent()
              .addStatement("paramParsers[$N++] = $N", position, token)
              .unindent()
              .add("else\n").indent()
              .addStatement("$N.add($N)", rest, token)
              .unindent();
        }
      } else {
        code.addStatement("paramParsers[$N++] = $N", position, token);
      }
    }

    // end parsing loop
    code.endControlFlow();

    return code.addStatement("return build()").build();
  }

  private CodeBlock optionBlock() {
    return CodeBlock.builder()
        .beginControlFlow("if (tryParseOption($N, $N))", token, it)
        .addStatement("continue")
        .endControlFlow()
        .build();
  }

  private CodeBlock.Builder initVariables() {
    CodeBlock.Builder code = CodeBlock.builder();
    if (!positionalParameters.none() && !(positionalParameters.anyRepeatable() && positionalParameters.size() == 1)) {
      code.addStatement("$T $N = $L", position.type, position, 0);
    }
    return code;
  }

  CodeBlock errorUnrecognizedOption() {
    return CodeBlock.builder().add("if ($N.matcher($N).matches())\n",
        commonFields.suspiciousPattern(), token).indent()
        .addStatement(throwInvalidOptionStatement("Invalid option"))
        .unindent().build();
  }

  private CodeBlock throwInvalidOptionStatement(String message) {
    return CodeBlock.of("throw new $T($S + $N)", RuntimeException.class, message + ": ", token);
  }
}
