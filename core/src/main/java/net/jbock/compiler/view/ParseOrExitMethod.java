package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.Collections;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.view.GeneratedClass.CONTINUATION_INDENT_USAGE;

@Reusable
public class ParseOrExitMethod extends Cached<MethodSpec> {

  private final SourceElement sourceElement;
  private final GeneratedTypes generatedTypes;
  private final CommonFields commonFields;
  private final PrintTokensMethod printTokensMethod;
  private final PrintOnlineHelpMethod printOnlineHelpMethod;

  @Inject
  ParseOrExitMethod(
      SourceElement sourceElement,
      GeneratedTypes generatedTypes,
      CommonFields commonFields,
      PrintTokensMethod printTokensMethod,
      PrintOnlineHelpMethod printOnlineHelpMethod) {
    this.sourceElement = sourceElement;
    this.generatedTypes = generatedTypes;
    this.commonFields = commonFields;
    this.printTokensMethod = printTokensMethod;
    this.printOnlineHelpMethod = printOnlineHelpMethod;
  }

  @Override
  MethodSpec define() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec result = builder(generatedTypes.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.addStatement("$T $N = parse($N)", result.type, result, args);

    code.add("if ($N instanceof $T)\n", result, generatedTypes.parsingSuccessWrapperType()).indent()
        .addStatement("return (($T) $N).$L()",
            generatedTypes.parsingSuccessWrapperType(),
            result,
            sourceElement.resultMethodName())
        .unindent();

    generatedTypes.helpRequestedType().ifPresent(helpRequestedType -> code
        .beginControlFlow("if ($N instanceof $T)", result, helpRequestedType)
        .addStatement("$N()", printOnlineHelpMethod.get())
        .addStatement("$N.flush()", commonFields.err())
        .addStatement("$N.accept($N)", commonFields.exitHook(), result)
        .addStatement("throw new $T($S)", RuntimeException.class, "help requested")
        .endControlFlow());

    code.addStatement("$N.println($S + (($T) $N).getError().getMessage())", commonFields.err(),
        "Error: ", generatedTypes.parsingFailedType(), result);
    if (sourceElement.helpEnabled()) {
      String blanks = String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " "));
      code.addStatement("$N($S, usage())", printTokensMethod.get(), blanks);
    } else {
      code.addStatement("$N()", printOnlineHelpMethod.get());
    }
    if (sourceElement.helpEnabled()) {
      code.addStatement("$N.println($S + $N + $S)", commonFields.err(),
          "Try '", commonFields.programName(), " --help' for more information.");
    }
    code.addStatement("$N.flush()", commonFields.err())
        .addStatement("$N.accept($N)", commonFields.exitHook(), result)
        .addStatement("throw new $T($S)", RuntimeException.class, "parsing error");

    return methodBuilder("parseOrExit").addParameter(args)
        .addModifiers(sourceElement.accessModifiers())
        .returns(generatedTypes.parseSuccessType())
        .addCode(code.build())
        .build();
  }
}
