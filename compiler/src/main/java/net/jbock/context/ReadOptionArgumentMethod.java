package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import javax.inject.Inject;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static java.util.Arrays.asList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class ReadOptionArgumentMethod extends CachedMethod {

  @Inject
  ReadOptionArgumentMethod() {
  }

  @Override
  MethodSpec define() {
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder code = CodeBlock.builder();
    ParameterSpec unix = builder(BOOLEAN, "unix").build();
    code.addStatement("$T $N = !$N.startsWith($S)", BOOLEAN, unix, token, "--");

    code.add("if ($N && $N.length() >= 3)\n", unix, token).indent()
        .addStatement("return $N.substring(2)", token).unindent();

    code.add("if (!$N && $N.contains($S))\n", unix, token, "=").indent()
        .addStatement("return $1N.substring($1N.indexOf('=') + 1)", token).unindent();

    code.add("if (!$N.hasNext())\n", it).indent()
        .addStatement("throw new $T($T.$L, $N)", ExToken.class,
            ErrTokenType.class, ErrTokenType.MISSING_ARGUMENT, token)
        .unindent();

    code.addStatement("return $N.next()", it);
    return methodBuilder("readOptionArgument")
        .addException(ExToken.class)
        .addCode(code.build())
        .addParameters(asList(token, it))
        .returns(STRING)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }
}
