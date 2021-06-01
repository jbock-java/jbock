package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static com.squareup.javapoet.TypeName.CHAR;
import static com.squareup.javapoet.TypeName.INT;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

class ReadTokenFromAtFileMethod extends Cached<MethodSpec> {

  @Inject
  ReadTokenFromAtFileMethod() {
  }

  @Override
  MethodSpec define() {
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    ParameterSpec esc = ParameterSpec.builder(BOOLEAN, "esc").build();
    ParameterSpec line = ParameterSpec.builder(STRING, "line").build();
    ParameterSpec c = ParameterSpec.builder(CHAR, "c").build();
    ParameterSpec i = ParameterSpec.builder(INT, "i").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $N.next()", STRING, line, it);
    code.addStatement("$1T $2N = new $1T()", StringBuilder.class, sb);
    code.beginControlFlow("while (true)");
    code.addStatement("$T $N = false", BOOLEAN, esc);

    code.beginControlFlow("for ($1T $2N = 0; $2N < $3N.length(); $2N++)", INT, i, line);
    code.addStatement("$T $N = $N.charAt($N)", CHAR, c, line, i);
    code.beginControlFlow("if ($N == '\\\\')", c);
    code.addStatement("if ($N) $N.append('\\\\')", esc, sb);
    code.addStatement("$1N = !$1N", esc);
    code.addStatement("continue");
    code.endControlFlow();
    code.beginControlFlow("if ($N)", esc);
    code.addStatement("if ($N == 'n') $N.append('\\n')", c, sb);
    code.addStatement("else if ($N == 'r') $N.append('\\r')", c, sb);
    code.addStatement("else if ($N == 't') $N.append('\\t')", c, sb);
    code.addStatement("else $N.append($N)", sb, c);
    code.addStatement("$N = false", esc);
    code.endControlFlow();
    code.addStatement("else $N.append($N)", sb, c);
    code.endControlFlow();

    code.addStatement("if (!$N || !$N.hasNext()) break", esc, it);
    code.addStatement("$N = $N.next()", line, it);
    code.endControlFlow();
    code.addStatement("return $N.toString()", sb);
    return MethodSpec.methodBuilder("readTokenFromAtFile")
        .addModifiers(Modifier.PRIVATE)
        .addCode(code.build())
        .addParameter(it)
        .returns(STRING)
        .build();
  }
}
