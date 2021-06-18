package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.either.Either;
import net.jbock.processor.SourceElement;
import net.jbock.util.AtFileReader;
import net.jbock.util.ExNotSuccess;
import net.jbock.util.FileReadingError;
import net.jbock.util.HelpRequested;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING_ARRAY;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class ParseMethod extends CachedMethod {

  private final GeneratedTypes generatedTypes;
  private final AllItems allItems;
  private final SourceElement sourceElement;
  private final BuildMethod buildMethod;
  private final CreateModelMethod createModelMethod;

  @Inject
  ParseMethod(
      GeneratedTypes generatedTypes,
      AllItems allItems,
      SourceElement sourceElement,
      BuildMethod buildMethod,
      CreateModelMethod createModelMethod) {
    this.generatedTypes = generatedTypes;
    this.allItems = allItems;
    this.sourceElement = sourceElement;
    this.buildMethod = buildMethod;
    this.createModelMethod = createModelMethod;
  }

  @Override
  MethodSpec define() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec err = builder(FileReadingError.class, "err").build();
    ParameterSpec either = builder(ParameterizedTypeName.get(
        ClassName.get(Either.class),
        ClassName.get(FileReadingError.class),
        LIST_OF_STRING),
        "either").build();
    ParameterSpec atFile = builder(BOOLEAN, "atFile").build();

    CodeBlock.Builder code = CodeBlock.builder();

    if (sourceElement.helpEnabled()) {
      if (allItems.anyRequired()) {
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

    if (sourceElement.atFileExpansion()) {
      code.addStatement(CodeBlock.builder()
          .add("$T $N = $N.length >= 1\n", BOOLEAN, atFile, args)
          .indent().indent().indent().indent()
          .add("&& $N[0].length() >= 2\n", args)
          .add("&& $N[0].startsWith($S)", args, "@")
          .unindent().unindent().unindent().unindent().build());
      code.addStatement(CodeBlock.builder()
          .add("$T $N = $N ?\n", either.type, either, atFile)
          .indent()
          .add("new $T().readAtFile($N) :\n", AtFileReader.class, args)
          .add("$T.right($T.asList($N))", Either.class, Arrays.class, args)
          .unindent().build());
      code.addStatement("return $L", CodeBlock.builder()
          .add("$1N.mapLeft($2N -> $2N.addModel($3N()))\n", either, err, createModelMethod.get()).indent()
          .add(".map($T::iterator)\n", List.class)
          .add(".flatMap($N -> {\n", it)
          .indent().add(coreBlock(it)).unindent()
          .add("})").unindent()
          .build());
    } else {
      code.addStatement("$T $N = $T.asList($N)", it.type, it,
          Either.class, Arrays.class, args);
      code.add(coreBlock(it));
    }

    return MethodSpec.methodBuilder("parse")
        .addParameter(args)
        .varargs(true)
        .returns(generatedTypes.parseResultType())
        .addCode(code.build())
        .addModifiers(sourceElement.accessModifiers())
        .build();
  }

  private CodeBlock coreBlock(ParameterSpec it) {
    ParameterSpec state = builder(generatedTypes.statefulParserType(), "statefulParser").build();
    ParameterSpec e = builder(Exception.class, "e").build();
    return CodeBlock.builder().add("$T $N = new $T();\n", state.type, state, state.type)
        .add("try {\n").indent()
        .add("return $T.right($N.parse($N).$N());\n", Either.class, state, it, buildMethod.get())
        .unindent().add("} catch ($T $N) {\n", ExNotSuccess.class, e).indent()
        .add("return $T.left($N.toError($N()));\n",
            Either.class, e, createModelMethod.get())
        .unindent().add("}\n")
        .build();
  }
}
