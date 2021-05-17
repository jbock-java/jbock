package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.qualifier.CommonFields;

import javax.inject.Inject;
import java.util.ArrayList;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

@Reusable
public class MakeLinesMethod {

  private final CommonFields commonFields;

  @Inject
  MakeLinesMethod(CommonFields commonFields) {
    this.commonFields = commonFields;
  }

  MethodSpec define() {
    ParameterSpec result = builder(LIST_OF_STRING, "result").build();
    ParameterSpec continuationIndent = builder(STRING, "continuationIndent").build();
    ParameterSpec i = builder(INT, "i").build();
    ParameterSpec fresh = builder(BOOLEAN, "fresh").build();
    ParameterSpec line = builder(StringBuilder.class, "line").build();
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T<>()", result.type, result, ArrayList.class);
    code.addStatement("$T $N = new $T()", line.type, line, StringBuilder.class);
    code.addStatement("$T $N = $L", INT, i, 0);
    code.beginControlFlow("while ($N < $N.size())", i, tokens);
    code.addStatement("$T $N = $N.get($N)", STRING, token, tokens, i);
    code.addStatement("$T $N = $N.length() == $L", BOOLEAN, fresh, line, 0);
    code.beginControlFlow("if (!$N && $N.length() + $N.length() + 1 > $N)",
        fresh, token, line, commonFields.terminalWidth());
    code.addStatement("$N.add($N.toString())", result, line);
    code.addStatement("$N.setLength(0)", line);
    code.addStatement("continue");
    code.endControlFlow();
    code.beginControlFlow("if ($N > 0)", i)
        .addStatement("$N.append($N ? $N : $S)", line, fresh, continuationIndent, " ")
        .endControlFlow();
    code.addStatement("$N.append($N)", line, token);
    code.addStatement("$N++", i);
    code.endControlFlow();

    code.beginControlFlow("if ($N.length() > 0)", line)
        .addStatement("$N.add($N.toString())", result, line)
        .endControlFlow();
    code.addStatement("return $N", result);
    return methodBuilder("makeLines")
        .addModifiers(PRIVATE)
        .addCode(code.build())
        .addParameter(continuationIndent)
        .addParameter(tokens)
        .returns(LIST_OF_STRING)
        .build();
  }
}
