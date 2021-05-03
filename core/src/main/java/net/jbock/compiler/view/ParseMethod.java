package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;

import javax.inject.Inject;
import java.util.ArrayList;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.view.GeneratedClass.SUSPICIOUS_PATTERN;

class ParseMethod {

  private final Context context;
  private final GeneratedTypes generatedTypes;

  private final ParameterSpec it;
  private final ParameterSpec token;
  private final ParameterSpec position;

  @Inject
  ParseMethod(Context context, GeneratedTypes generatedTypes) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.it = builder(STRING_ITERATOR, "it").build();
    this.token = builder(STRING, "token").build();
    this.position = builder(INT, "position").build();
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
    ParameterSpec rest = builder(LIST_OF_STRING, "rest").build();
    code.addStatement("$T $N = new $T<>()", rest.type, rest, ArrayList.class);

    // begin parsing loop
    code.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it);

    code.beginControlFlow("if ($N == $L)", position, context.params().size())
        .addStatement("$N.add($N)", rest, token)
        .addStatement("continue")
        .endControlFlow();

    if (!context.options().isEmpty()) {
      code.add(optionBlock());
    }
    code.add(errorUnrecognizedOption());

    code.addStatement("paramParsers[$N].read($N)", position, token)
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

    if (!context.options().isEmpty()) {
      code.add(optionBlock());
    }
    code.beginControlFlow("if (!$N)", endOfOptionParsing)
        .add(errorUnrecognizedOption())
        .endControlFlow();

    if (context.params().isEmpty()) {
      code.addStatement(throwInvalidOptionStatement("Excess param"));
    } else if (!context.anyRepeatableParam()) {
      code.add("if ($N == $L)\n", position, context.params().size()).indent()
          .addStatement(throwInvalidOptionStatement("Excess param"))
          .unindent();
    }

    if (!context.params().isEmpty()) {
      code.addStatement("paramParsers[$N].read($N)", position, token);
      if (context.anyRepeatableParam() && context.params().size() >= 2) {
        code.add("if ($N < $L)\n", position, context.params().size() - 1).indent()
            .addStatement("$N++", position)
            .unindent();
      } else if (!context.anyRepeatableParam()) {
        code.addStatement("$N++", position);
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
    if (!context.params().isEmpty()) {
      code.addStatement("$T $N = $L", position.type, position, 0);
    }
    return code;
  }

  CodeBlock errorUnrecognizedOption() {
    return CodeBlock.builder().add("if ($L.matcher($N).matches())\n",
        SUSPICIOUS_PATTERN, token).indent()
        .addStatement(throwInvalidOptionStatement("Invalid option"))
        .unindent().build();
  }

  private CodeBlock throwInvalidOptionStatement(String message) {
    return CodeBlock.of("throw new $T($S + $N)", RuntimeException.class, message + ": ", token);
  }
}
