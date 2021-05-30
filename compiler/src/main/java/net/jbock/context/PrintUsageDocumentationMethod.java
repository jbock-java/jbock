package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.common.SafeElements;
import net.jbock.processor.SourceElement;
import net.jbock.convert.ConvertedParameter;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;
import static net.jbock.context.GeneratedClass.CONTINUATION_INDENT_USAGE;

@ContextScope
public class PrintUsageDocumentationMethod extends Cached<MethodSpec> {

  private static final String USAGE = "USAGE";
  private static final String PARAMETERS = "PARAMETERS";
  private static final String OPTIONS = "OPTIONS";

  private final SourceElement sourceElement;
  private final AllParameters allParameters;
  private final PositionalParameters positionalParameters;
  private final NamedOptions namedOptions;
  private final MakeLinesMethod makeLinesMethod;
  private final CommonFields commonFields;
  private final SafeElements elements;
  private final PrintItemDocumentationMethod printItemDocumentationMethod;
  private final UsageMethod usageMethod;
  private final AnsiStyle styler;
  private final String paramsFormat;
  private final String optionsFormat;
  private final ParameterSpec optionsIndent = ParameterSpec.builder(STRING, "indent_o").build();
  private final ParameterSpec paramsIndent = ParameterSpec.builder(STRING, "indent_p").build();

  @Inject
  PrintUsageDocumentationMethod(
      SourceElement sourceElement,
      AllParameters allParameters,
      PositionalParameters positionalParameters,
      NamedOptions namedOptions,
      MakeLinesMethod makeLinesMethod,
      CommonFields commonFields,
      SafeElements elements,
      PrintItemDocumentationMethod printItemDocumentationMethod,
      UsageMethod usageMethod,
      AnsiStyle styler) {
    this.sourceElement = sourceElement;
    this.allParameters = allParameters;
    this.positionalParameters = positionalParameters;
    this.namedOptions = namedOptions;
    this.makeLinesMethod = makeLinesMethod;
    this.commonFields = commonFields;
    this.elements = elements;
    this.printItemDocumentationMethod = printItemDocumentationMethod;
    this.usageMethod = usageMethod;
    this.styler = styler;
    this.optionsFormat = "  %1$-" + namedOptions.maxWidth() + "s ";
    this.paramsFormat = "  %1$-" + positionalParameters.maxWidth() + "s ";
  }

  @Override
  MethodSpec define() {
    CodeBlock.Builder code = CodeBlock.builder();
    String continuationIndent = String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " "));

    List<String> description = sourceElement.description(elements);
    if (!description.isEmpty()) {
      ParameterSpec descriptionBuilder = builder(LIST_OF_STRING, "description").build();
      code.addStatement("$T $N = new $T<>()", descriptionBuilder.type, descriptionBuilder, ArrayList.class);
      sourceElement.descriptionKey()
          .ifPresentOrElse(key -> {
            ParameterSpec descriptionMessage = builder(STRING, "descriptionMessage").build();
            code.addStatement("$T $N = messages.get($S)", STRING, descriptionMessage, key);
            code.beginControlFlow("if ($N != null)", descriptionMessage)
                .addStatement("$T.addAll($N, $N.split($S, $L))",
                    Collections.class, descriptionBuilder, descriptionMessage, "\\s+", -1);
            code.endControlFlow();
            code.beginControlFlow("else");
            for (String line : description) {
              code.addStatement("$T.addAll($N, $S.split($S, $L))",
                  Collections.class, descriptionBuilder, line, "\\s+", -1);
            }
            code.endControlFlow();
          }, () -> {
            for (String line : description) {
              code.addStatement("$T.addAll($N, $S.split($S, $L))",
                  Collections.class, descriptionBuilder, line, "\\s+", -1);
            }
          });
      code.addStatement("$N($S, $N).forEach($N::println)", makeLinesMethod.get(), "",
          descriptionBuilder, commonFields.err());
      code.addStatement("$N.println()", commonFields.err());
    }

    code.addStatement("$N.println($S)", commonFields.err(), styler.bold(USAGE).orElse(USAGE));
    code.addStatement("$N($S, $N($S)).forEach($N::println)", makeLinesMethod.get(), continuationIndent,
        usageMethod.get(), " ", commonFields.err());

    if (!positionalParameters.isEmpty()) {
      code.addStatement("$N.println()", commonFields.err());
      code.addStatement("$N.println($S)", commonFields.err(), styler.bold(PARAMETERS).orElse(PARAMETERS));
      code.addStatement("$T $N = $S", STRING, paramsIndent, String.join("", Collections.nCopies(positionalParameters.maxWidth() + 4, " ")));
    }
    positionalParameters.forEachRegular(p -> code.add(printPositionalCode(p)));
    positionalParameters.repeatable().ifPresent(p -> code.add(printPositionalCode(p)));
    if (!namedOptions.isEmpty()) {
      code.addStatement("$N.println()", commonFields.err());
      code.addStatement("$N.println($S)", commonFields.err(), styler.bold(OPTIONS).orElse(OPTIONS));
      code.addStatement("$T $N = $S", STRING, optionsIndent, String.join("", Collections.nCopies(namedOptions.maxWidth() + 4, " ")));
    }

    namedOptions.forEach(c -> code.add(printNamedOptionCode(c)));
    return methodBuilder("printUsageDocumentation")
        .addModifiers(sourceElement.accessModifiers())
        .addCode(code.build())
        .build();
  }

  private CodeBlock printNamedOptionCode(ConvertedParameter<NamedOption> c) {
    String enumConstant = c.enumConstant();
    if (allParameters.anyDescriptionKeys()) {
      return CodeBlock.builder().addStatement("$N($T.$L, $S, $N, $S)",
          printItemDocumentationMethod.get(),
          sourceElement.itemType(), enumConstant,
          String.format(optionsFormat, c.parameter().namesWithLabel(c.isFlag())),
          optionsIndent,
          c.parameter().descriptionKey().orElse("")).build();
    } else {
      return CodeBlock.builder().addStatement("$N($T.$L, $S, $N)",
          printItemDocumentationMethod.get(),
          sourceElement.itemType(), enumConstant,
          String.format(optionsFormat, c.parameter().namesWithLabel(c.isFlag())),
          optionsIndent).build();
    }
  }

  private CodeBlock printPositionalCode(ConvertedParameter<PositionalParameter> c) {
    String enumConstant = c.enumConstant();
    if (allParameters.anyDescriptionKeys()) {
      return CodeBlock.builder().addStatement("$N($T.$L, $S, $N, $S)",
          printItemDocumentationMethod.get(),
          sourceElement.itemType(), enumConstant,
          String.format(paramsFormat, c.paramLabel()),
          paramsIndent,
          c.parameter().descriptionKey().orElse("")).build();
    } else {
      return CodeBlock.builder().addStatement("$N($T.$L, $S, $N)",
          printItemDocumentationMethod.get(),
          sourceElement.itemType(), enumConstant,
          String.format(paramsFormat, c.paramLabel()),
          paramsIndent).build();
    }
  }
}
