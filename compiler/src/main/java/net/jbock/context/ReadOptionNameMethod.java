package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.inject.Inject;

import static net.jbock.common.Constants.STRING;

@ContextScope
public class ReadOptionNameMethod extends CachedMethod {

  @Inject
  ReadOptionNameMethod() {
  }

  @Override
  MethodSpec define() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    CodeBlock.Builder code = CodeBlock.builder();
    code.add("if ($1N.length() < 2 || !$1N.startsWith($2S))\n", token, "-").indent()
        .addStatement("return null").unindent();

    code.add("if (!$N.startsWith($S))\n", token, "--").indent()
        .addStatement("return $N.substring(0, 2)", token).unindent();

    code.add("if (!$N.contains($S))\n", token, "=").indent()
        .addStatement("return $N", token).unindent();

    code.addStatement("return $1N.substring(0, $1N.indexOf('='))", token);

    return MethodSpec.methodBuilder("readOptionName")
        .addParameter(token)
        .addCode(code.build())
        .returns(STRING).build();
  }
}
