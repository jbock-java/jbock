package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.inject.Inject;

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class ReadOptionNameMethod extends CachedMethod {

  @Inject
  ReadOptionNameMethod() {
  }

  @Override
  MethodSpec define() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec index = ParameterSpec.builder(INT, "index").build();

    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($N.length() <= 1 || $N.charAt(0) != '-')\n", token, token).indent()
        .addStatement("return null").unindent();

    code.add("if (!$N.startsWith($S))\n", token, "--").indent()
        .addStatement("return $N.substring(0, 2)", token).unindent();

    code.addStatement("$T $N = $N.indexOf('=')", INT, index, token);
    code.add("if ($N < 0)\n", index).indent()
        .addStatement("return $N", token)
        .unindent();
    code.addStatement("return $N.substring(0, $N)", token, index);

    return MethodSpec.methodBuilder("readOptionName")
        .addParameter(token)
        .addCode(code.build())
        .addModifiers(STATIC, PRIVATE)
        .returns(STRING).build();
  }
}
