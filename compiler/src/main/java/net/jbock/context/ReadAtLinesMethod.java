package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collections;

import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

class ReadAtLinesMethod extends Cached<MethodSpec> {

  private final ReadTokenFromAtFileMethod readTokenFromAtFileMethod;

  @Inject
  ReadAtLinesMethod(ReadTokenFromAtFileMethod readTokenFromAtFileMethod) {
    this.readTokenFromAtFileMethod = readTokenFromAtFileMethod;
  }

  @Override
  MethodSpec define() {
    CodeBlock.Builder code = CodeBlock.builder();
    ParameterSpec lines = ParameterSpec.builder(LIST_OF_STRING, "lines").build();
    ParameterSpec copy = ParameterSpec.builder(LIST_OF_STRING, "copy").build();
    ParameterSpec tokens = ParameterSpec.builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    code.addStatement("$1N = new $2T<>($1N)", lines, ArrayList.class);
    code.addStatement("$T.reverse($N)", Collections.class, lines);
    code.addStatement("$T $N = new $T<>($N.size())", LIST_OF_STRING, copy, ArrayList.class, lines);
    code.addStatement("$N.stream().dropWhile($T::isEmpty).forEach($N::add)", lines, STRING, copy);
    code.addStatement("$T.reverse($N)", Collections.class, copy);
    code.addStatement("$T $N = $N.iterator()", STRING_ITERATOR, it, copy);
    code.addStatement("$T $N = new $T<>($N.size())", LIST_OF_STRING, tokens, ArrayList.class, copy);
    code.beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$N.add($N($N))", tokens, readTokenFromAtFileMethod.get(), it)
        .endControlFlow();
    code.addStatement("return $N", tokens);
    return MethodSpec.methodBuilder("readAtLines")
        .returns(LIST_OF_STRING)
        .addCode(code.build())
        .addParameter(lines)
        .addModifiers(Modifier.PRIVATE)
        .build();
  }
}
