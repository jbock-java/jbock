package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.either.Either;
import net.jbock.processor.SourceElement;
import net.jbock.util.AtFileReader;
import net.jbock.util.ConverterError;
import net.jbock.util.HelpRequested;
import net.jbock.util.SyntaxError;

import javax.inject.Inject;
import java.util.Arrays;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.common.Constants.STRING_ARRAY;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class ParseMethod extends Cached<MethodSpec> {

  private final GeneratedTypes generatedTypes;
  private final AllParameters allParameters;
  private final SourceElement sourceElement;
  private final BuildMethod buildMethod;
  private final CommonFields commonFields;
  private final CreateModelMethod createModelMethod;

  @Inject
  ParseMethod(
      GeneratedTypes generatedTypes,
      AllParameters allParameters,
      SourceElement sourceElement,
      BuildMethod buildMethod,
      CommonFields commonFields,
      CreateModelMethod createModelMethod) {
    this.generatedTypes = generatedTypes;
    this.allParameters = allParameters;
    this.sourceElement = sourceElement;
    this.buildMethod = buildMethod;
    this.commonFields = commonFields;
    this.createModelMethod = createModelMethod;
  }

  @Override
  MethodSpec define() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec e = builder(Exception.class, "e").build();
    CodeBlock.Builder code = CodeBlock.builder();

    if (sourceElement.helpEnabled()) {
      if (allParameters.anyRequired()) {
        code.add("if ($N.length == 0)\n", args).indent()
            .addStatement("return $T.left(new $T($N()))", Either.class, HelpRequested.class,
                createModelMethod.get())
            .unindent();
      }
      code.add("if ($1N.length == 1 && $2S.equals($1N[0]))\n", args, "--help").indent()
          .addStatement("return $T.left(new $T($N()))", Either.class, HelpRequested.class,
              createModelMethod.get())
          .unindent();
    }
    ParameterSpec state = builder(generatedTypes.statefulParserType(), "statefulParser").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec atFile = builder(BOOLEAN, "atFile").build();
    code.addStatement("$T $N = new $T()", state.type, state, state.type);
    code.beginControlFlow("try");
    if (sourceElement.expandAtSign()) {
      code.addStatement(CodeBlock.builder()
          .add("$T $N = $N.length == 1\n", BOOLEAN, atFile, args)
          .indent().indent().indent().indent()
          .add("&& $N[0].length() >= 2\n", args)
          .add("&& $N[0].startsWith($S)", args, "@")
          .unindent().unindent().unindent().unindent().build());
      code.addStatement(CodeBlock.builder()
          .add("$T $N = $N ?\n", STRING_ITERATOR, it, atFile)
          .indent()
          .add("new $T().readAtFile($N[0].substring(1)).iterator() :\n", AtFileReader.class, args)
          .add("$T.asList($N).iterator()", Arrays.class, args)
          .unindent().build());
    } else {
      code.addStatement("$T $N = $T.asList($N).iterator()", it.type, it, Arrays.class, args);
    }
    code.addStatement("return $T.right($N.parse($N).$N())", Either.class, state, it, buildMethod.get());
    code.endControlFlow();

    code.beginControlFlow("catch ($T $N)", generatedTypes.convExType(), e)
        .addStatement("return $1T.left(new $2T($3N(), $4N.$5N, $4N.$6N, $4N.$7N))",
            Either.class,
            ConverterError.class, createModelMethod.get(), e,
            commonFields.convExFailure(), commonFields.convExItemType(), commonFields.convExItemName())
        .endControlFlow();

    code.beginControlFlow("catch ($T $N)", Exception.class, e)
        .addStatement("return $T.left(new $T($N(), $N.getMessage()))", Either.class,
            SyntaxError.class, createModelMethod.get(), e)
        .endControlFlow();

    return MethodSpec.methodBuilder("parse").addParameter(args)
        .returns(generatedTypes.parseResultType())
        .addCode(code.build())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }
}
