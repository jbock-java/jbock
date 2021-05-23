package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.color.Styler;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.util.Collections;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.view.GeneratedClass.CONTINUATION_INDENT_USAGE;

@Reusable
public class ParseOrExitMethod {

  private final SourceElement sourceElement;
  private final GeneratedTypes generatedTypes;
  private final CommonFields commonFields;
  private final PrintTokensMethod printTokensMethod;
  private final PrintOnlineHelpMethod printOnlineHelpMethod;
  private final UsageMethod usageMethod;
  private final ParseMethod parseMethod;
  private final Styler styler;

  @Inject
  ParseOrExitMethod(
      SourceElement sourceElement,
      GeneratedTypes generatedTypes,
      CommonFields commonFields,
      PrintTokensMethod printTokensMethod,
      PrintOnlineHelpMethod printOnlineHelpMethod,
      UsageMethod usageMethod,
      ParseMethod parseMethod,
      Styler styler) {
    this.sourceElement = sourceElement;
    this.generatedTypes = generatedTypes;
    this.commonFields = commonFields;
    this.printTokensMethod = printTokensMethod;
    this.printOnlineHelpMethod = printOnlineHelpMethod;
    this.usageMethod = usageMethod;
    this.parseMethod = parseMethod;
    this.styler = styler;
  }

  MethodSpec get() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec result = builder(generatedTypes.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.addStatement("$T $N = $N($N)", result.type, result, parseMethod.get(), args);

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
        styler.boldRed("ERROR").orElse("ERROR:") + " ", generatedTypes.parsingFailedType(), result);
    if (sourceElement.helpEnabled()) {
      String blanks = String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " "));
      code.addStatement("$N($S, $N($S))", printTokensMethod.get(), blanks, usageMethod.get(),
          "Usage:");
    } else {
      code.addStatement("$N()", printOnlineHelpMethod.get());
    }
    if (sourceElement.helpEnabled()) {
      String helpSuggestion = sourceElement.programName() + " --help";
      code.addStatement("$N.println($S)", commonFields.err(),
          "Type " + styler.yellow(helpSuggestion).orElseGet(() -> '"' + helpSuggestion + '"') +
              " for more information.");
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
