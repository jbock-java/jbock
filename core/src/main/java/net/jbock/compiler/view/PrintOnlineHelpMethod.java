package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.color.Styler;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.view.GeneratedClass.CONTINUATION_INDENT_USAGE;

@Reusable
public class PrintOnlineHelpMethod extends Cached<MethodSpec> {

  private static final String USAGE = "USAGE";
  private static final String PARAMETERS = "PARAMETERS";
  private static final String OPTIONS = "OPTIONS";

  private final SourceElement sourceElement;
  private final AllParameters allParameters;
  private final PositionalParameters positionalParameters;
  private final NamedOptions namedOptions;
  private final PrintTokensMethod printTokensMethod;
  private final CommonFields commonFields;
  private final Elements elements;
  private final PrintOptionMethod printOptionMethod;
  private final UsageMethod usageMethod;
  private final Styler styler;

  @Inject
  PrintOnlineHelpMethod(
      SourceElement sourceElement,
      AllParameters allParameters,
      PositionalParameters positionalParameters,
      NamedOptions namedOptions,
      PrintTokensMethod printTokensMethod,
      CommonFields commonFields,
      Elements elements,
      PrintOptionMethod printOptionMethod,
      UsageMethod usageMethod,
      Styler styler) {
    this.sourceElement = sourceElement;
    this.allParameters = allParameters;
    this.positionalParameters = positionalParameters;
    this.namedOptions = namedOptions;
    this.printTokensMethod = printTokensMethod;
    this.commonFields = commonFields;
    this.elements = elements;
    this.printOptionMethod = printOptionMethod;
    this.usageMethod = usageMethod;
    this.styler = styler;
  }

  @Override
  MethodSpec define() {
    CodeBlock.Builder code = CodeBlock.builder();
    String continuationIndent = String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " "));

    List<String> description = sourceElement.description(elements);
    if (!description.isEmpty()) {
      ParameterSpec descriptionBuilder = builder(LIST_OF_STRING, "description").build();
      code.addStatement("$T $N = new $T<>()", descriptionBuilder.type, descriptionBuilder, ArrayList.class);
      CodeBlock descriptionBlock = sourceElement.descriptionKey()
          .map(key -> {
            CodeBlock.Builder result = CodeBlock.builder();
            ParameterSpec descriptionMessage = builder(STRING, "descriptionMessage").build();
            result.addStatement("$T $N = messages.get($S)", STRING, descriptionMessage, key);
            result.beginControlFlow("if ($N != null)", descriptionMessage)
                .addStatement("$T.addAll($N, $N.split($S, $L))",
                    Collections.class, descriptionBuilder, descriptionMessage, "\\s+", -1);
            result.endControlFlow();
            result.beginControlFlow("else");
            for (String line : description) {
              result.addStatement("$T.addAll($N, $S.split($S, $L))",
                  Collections.class, descriptionBuilder, line, "\\s+", -1);
            }
            result.endControlFlow();
            return result.build();
          })
          .orElseGet(() -> {
            CodeBlock.Builder result = CodeBlock.builder();
            for (String line : description) {
              result.addStatement("$T.addAll($N, $S.split($S, $L))",
                  Collections.class, descriptionBuilder, line, "\\s+", -1);
            }
            return result.build();
          });
      code.add(descriptionBlock);
      code.addStatement("$N($S, $N)", printTokensMethod.get(), "", descriptionBuilder);
      code.addStatement("$N.println()", commonFields.err());
    }

    code.addStatement("$N.println($S)", commonFields.err(), styler.bold(USAGE).orElse(USAGE));
    code.addStatement("$N($S, $N($S))", printTokensMethod.get(), continuationIndent,
        usageMethod.get(), " ");

    String paramsFormat = "  %1$-" + positionalParameters.maxWidth() + "s ";

    if (!positionalParameters.none()) {
      code.addStatement("$N.println()", commonFields.err());
      code.addStatement("$N.println($S)", commonFields.err(), styler.bold(PARAMETERS).orElse(PARAMETERS));
    }
    positionalParameters.forEachRegular(p -> code.add(printPositionalCode(paramsFormat, p)));
    positionalParameters.repeatable().ifPresent(p -> code.add(printPositionalCode(paramsFormat, p)));
    if (!namedOptions.isEmpty()) {
      code.addStatement("$N.println()", commonFields.err());
      code.addStatement("$N.println($S)", commonFields.err(), styler.bold(OPTIONS).orElse(OPTIONS));
    }

    String optionsFormat = "  %1$-" + namedOptions.maxWidth() + "s ";

    namedOptions.forEach(c -> code.add(printNamedOptionCode(optionsFormat, c)));
    return methodBuilder("printOnlineHelp")
        .addModifiers(sourceElement.accessModifiers())
        .addCode(code.build())
        .build();
  }

  private CodeBlock printNamedOptionCode(String optionsFormat, ConvertedParameter<NamedOption> c) {
    String enumConstant = c.enumConstant();
    if (allParameters.anyDescriptionKeys()) {
      return CodeBlock.builder().addStatement("$N($T.$L, $S, $S)",
          printOptionMethod.get(),
          sourceElement.optionType(), enumConstant,
          String.format(optionsFormat, c.parameter().namesWithLabel(c.isFlag())),
          c.parameter().descriptionKey().orElse("")).build();
    } else {
      return CodeBlock.builder().addStatement("$N($T.$L, $S)",
          printOptionMethod.get(),
          sourceElement.optionType(), enumConstant,
          String.format(optionsFormat, c.parameter().namesWithLabel(c.isFlag()))).build();
    }
  }

  private CodeBlock printPositionalCode(String paramsFormat, ConvertedParameter<PositionalParameter> c) {
    String enumConstant = c.enumConstant();
    if (allParameters.anyDescriptionKeys()) {
      return CodeBlock.builder().addStatement("$N($T.$L, $S, $S)",
          printOptionMethod.get(),
          sourceElement.optionType(), enumConstant,
          String.format(paramsFormat, c.paramLabel()),
          c.parameter().descriptionKey().orElse("")).build();
    } else {
      return CodeBlock.builder().addStatement("$N($T.$L, $S)",
          printOptionMethod.get(),
          sourceElement.optionType(), enumConstant,
          String.format(paramsFormat, c.paramLabel())).build();
    }
  }
}
