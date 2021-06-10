package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.util.ExToken;
import net.jbock.util.ErrTokenType;

import javax.inject.Inject;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

public class TryParseOptionMethod extends Cached<MethodSpec> {

  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final CommonFields commonFields;
  private final ReadOptionNameMethod readOptionNameMethod;

  @Inject
  TryParseOptionMethod(
      SourceElement sourceElement,
      NamedOptions namedOptions,
      CommonFields commonFields,
      ReadOptionNameMethod readOptionNameMethod) {
    this.sourceElement = sourceElement;
    this.namedOptions = namedOptions;
    this.commonFields = commonFields;
    this.readOptionNameMethod = readOptionNameMethod;
  }

  @Override
  MethodSpec define() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("tryParseOption")
        .addException(ExToken.class)
        .addParameter(token)
        .addParameter(it)
        .addCode(namedOptions.unixClusteringSupported() ?
            tryParseOptionCodeClustering(token, it) :
            tryParseOptionCodeSimple(token, it))
        .returns(BOOLEAN)
        .build();
  }

  private CodeBlock tryParseOptionCodeClustering(ParameterSpec token, ParameterSpec it) {
    ParameterSpec clusterToken = ParameterSpec.builder(STRING, "clusterToken").build();
    ParameterSpec option = ParameterSpec.builder(sourceElement.optionEnumType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $N.get($N($N))", sourceElement.optionEnumType(),
        option, commonFields.optionNames(), readOptionNameMethod.get(), token);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("return false")
        .unindent();
    code.addStatement("$T $N = $N", clusterToken.type, clusterToken, token);
    code.beginControlFlow("while ($N.get($N).read($N, $N))", commonFields.optionParsers(), option, clusterToken, it);
    code.addStatement("$1N = '-' + $1N.substring(2, $1N.length())", clusterToken);
    code.addStatement("$N = $N.get($N($N))", option, commonFields.optionNames(), readOptionNameMethod.get(),
        clusterToken);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("throw new $T($T.$L, $N)", ExToken.class, ErrTokenType.class,
            ErrTokenType.INVALID_TOKEN, token)
        .unindent();
    code.endControlFlow();
    code.addStatement("return true");
    return code.build();
  }

  private CodeBlock tryParseOptionCodeSimple(ParameterSpec token, ParameterSpec it) {
    ParameterSpec option = ParameterSpec.builder(sourceElement.optionEnumType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $N.get($N($N))", sourceElement.optionEnumType(), option,
        commonFields.optionNames(), readOptionNameMethod.get(), token);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("return false")
        .unindent();
    code.addStatement("$N.get($N).read($N, $N)", commonFields.optionParsers(), option, token, it)
        .addStatement("return true");
    return code.build();
  }
}
