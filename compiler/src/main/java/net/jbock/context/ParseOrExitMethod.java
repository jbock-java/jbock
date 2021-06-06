package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;
import net.jbock.usage.Synopsis;
import net.jbock.usage.UsageDocumentation;

import javax.inject.Inject;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class ParseOrExitMethod {

  private final SourceElement sourceElement;
  private final GeneratedTypes generatedTypes;
  private final CommonFields commonFields;
  private final ParseMethod parseMethod;
  private final AnsiStyle styler;

  @Inject
  ParseOrExitMethod(
      SourceElement sourceElement,
      GeneratedTypes generatedTypes,
      CommonFields commonFields,
      ParseMethod parseMethod,
      AnsiStyle styler) {
    this.sourceElement = sourceElement;
    this.generatedTypes = generatedTypes;
    this.commonFields = commonFields;
    this.parseMethod = parseMethod;
    this.styler = styler;
  }

  MethodSpec get() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec notSuccess = builder(generatedTypes.parseResultType(), "notSuccess").build();

    CodeBlock.Builder code = CodeBlock.builder();
    generatedTypes.helpRequestedType().ifPresent(helpRequestedType -> code
        .beginControlFlow("if ($N instanceof $T)", notSuccess, helpRequestedType)
        .add("$T.builder($N.commandModel())\n", UsageDocumentation.class, notSuccess).indent()
        .add(".build().printUsageDocumentation();\n").unindent()
        .addStatement("$T.exit(0)", System.class)
        .endControlFlow());

    code.addStatement("$N.println($S + (($T) $N).message())", commonFields.err(),
        styler.red("ERROR:") + " ", generatedTypes.parsingFailedType(), notSuccess);
    if (sourceElement.helpEnabled()) {
      ParameterSpec synopsis = builder(LIST_OF_STRING, "synopsis").build();
      code.add("$T $N = $T.create($N.commandModel())\n", LIST_OF_STRING, synopsis, Synopsis.class, notSuccess).indent()
          .add(".createSynopsis($S);\n", "Usage:").unindent();
      code.addStatement("$N.println($T.join($S, $N))",
          commonFields.err(), STRING, " ", synopsis);
    } else {
      code.add("$T.builder($N.commandModel())\n", UsageDocumentation.class, notSuccess).indent()
          .add(".build().printUsageDocumentation();\n").unindent();
    }
    if (sourceElement.helpEnabled()) {
      String helpSuggestion = sourceElement.programName() + " --help";
      code.addStatement("$N.println($S)", commonFields.err(),
          "Type " + styler.bold(helpSuggestion).orElseGet(() -> '"' + helpSuggestion + '"') +
              " for more information.");
    }
    code.addStatement("$N.flush()", commonFields.err());
    code.addStatement("$T.exit(1)", System.class);
    code.addStatement("return new $T()", RuntimeException.class);

    return methodBuilder("parseOrExit").addParameter(args)
        .addModifiers(sourceElement.accessModifiers())
        .returns(generatedTypes.parseSuccessType())
        .addCode(CodeBlock.builder()
            .add("return $N($N)", parseMethod.get(), args)
            .add(".orElseThrow($N -> {\n", notSuccess).indent()
            .add(code.build()).unindent()
            .add("});").build())
        .build();
  }
}
