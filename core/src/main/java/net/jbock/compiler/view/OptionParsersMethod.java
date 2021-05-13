package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.NamedOptions;

import javax.inject.Inject;
import java.util.Collections;
import java.util.EnumMap;

import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.mapOf;

class OptionParsersMethod {

  private final GeneratedTypes generatedTypes;
  private final GeneratedType generatedType;
  private final NamedOptions namedOptions;

  @Inject
  OptionParsersMethod(
      GeneratedTypes generatedTypes,
      GeneratedType generatedType,
      NamedOptions namedOptions) {
    this.generatedTypes = generatedTypes;
    this.generatedType = generatedType;
    this.namedOptions = namedOptions;
  }

  MethodSpec define() {
    ParameterSpec parsers = builder(mapOf(generatedType.optionType(),
        generatedTypes.optionParserType()), "parsers").build();

    return MethodSpec.methodBuilder("optionParsers").returns(parsers.type)
        .addCode(optionParsersMethodCode(parsers))
        .addModifiers(PRIVATE, STATIC).build();
  }

  private CodeBlock optionParsersMethodCode(ParameterSpec parsers) {
    if (namedOptions.isEmpty()) {
      return CodeBlock.builder().addStatement("return $T.emptyMap()", Collections.class).build();
    }
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T<>($T.class)", parsers.type, parsers, EnumMap.class, generatedType.optionType());
    for (ConvertedParameter<NamedOption> param : namedOptions.options()) {
      String enumConstant = param.enumConstant();
      code.addStatement("$N.put($T.$L, new $T($T.$L))",
          parsers, generatedType.optionType(), enumConstant, optionParserType(param),
          generatedType.optionType(), enumConstant);
    }
    code.addStatement("return $N", parsers);
    return code.build();
  }

  private ClassName optionParserType(ConvertedParameter<NamedOption> param) {
    if (param.isRepeatable()) {
      return generatedTypes.repeatableOptionParserType();
    }
    if (param.isFlag()) {
      return generatedTypes.flagParserType();
    }
    return generatedTypes.regularOptionParserType();
  }
}
