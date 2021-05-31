package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class ParseOrExitMethod {

  private final SourceElement sourceElement;
  private final GeneratedTypes generatedTypes;
  private final CommonFields commonFields;
  private final PrintUsageDocumentationMethod printUsageDocumentationMethod;
  private final UsageMethod usageMethod;
  private final ParseMethod parseMethod;
  private final AnsiStyle styler;

  @Inject
  ParseOrExitMethod(
      SourceElement sourceElement,
      GeneratedTypes generatedTypes,
      CommonFields commonFields,
      PrintUsageDocumentationMethod printUsageDocumentationMethod,
      UsageMethod usageMethod,
      ParseMethod parseMethod,
      AnsiStyle styler) {
    this.sourceElement = sourceElement;
    this.generatedTypes = generatedTypes;
    this.commonFields = commonFields;
    this.printUsageDocumentationMethod = printUsageDocumentationMethod;
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
        .addStatement("$N()", printUsageDocumentationMethod.get())
        .addStatement("$N.flush()", commonFields.err())
        .addStatement("$N.accept($N)", commonFields.exitHook(), result)
        .addStatement("throw new $T($S)", RuntimeException.class, "help requested")
        .endControlFlow());

    code.addStatement("$N.println($S + (($T) $N).getError().getMessage())", commonFields.err(),
        styler.red("ERROR:") + " ", generatedTypes.parsingFailedType(), result);
    if (sourceElement.helpEnabled()) {
      code.addStatement("$N.println($T.join($S, $N($S)))",
          commonFields.err(), STRING, " ", usageMethod.get(), "Usage:");
    } else {
      code.addStatement("$N()", printUsageDocumentationMethod.get());
    }
    if (sourceElement.helpEnabled()) {
      String helpSuggestion = sourceElement.programName() + " --help";
      code.addStatement("$N.println($S)", commonFields.err(),
          "Type " + styler.bold(helpSuggestion).orElseGet(() -> '"' + helpSuggestion + '"') +
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
