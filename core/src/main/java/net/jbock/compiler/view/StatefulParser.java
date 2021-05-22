package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import dagger.Reusable;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

/**
 * Defines the inner class StatefulParser
 */
@Reusable
public class StatefulParser extends Cached<TypeSpec> {

  private final StatefulParseMethod statefulParseMethod;
  private final GeneratedTypes generatedTypes;
  private final SourceElement sourceElement;
  private final NamedOptions namedOptions;
  private final PositionalParameters positionalParameters;
  private final CommonFields commonFields;
  private final ReadOptionNameMethod readOptionNameMethod;
  private final BuildMethod buildMethod;

  @Inject
  StatefulParser(
      GeneratedTypes generatedTypes,
      StatefulParseMethod statefulParseMethod,
      SourceElement sourceElement,
      NamedOptions namedOptions,
      PositionalParameters positionalParameters,
      CommonFields commonFields,
      ReadOptionNameMethod readOptionNameMethod,
      BuildMethod buildMethod) {
    this.generatedTypes = generatedTypes;
    this.statefulParseMethod = statefulParseMethod;
    this.sourceElement = sourceElement;
    this.namedOptions = namedOptions;
    this.positionalParameters = positionalParameters;
    this.commonFields = commonFields;
    this.readOptionNameMethod = readOptionNameMethod;
    this.buildMethod = buildMethod;
  }

  @Override
  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.statefulParserType())
        .addModifiers(PRIVATE, STATIC)
        .addMethod(statefulParseMethod.parseMethod());
    spec.addField(commonFields.suspiciousPattern());
    if (!namedOptions.isEmpty()) {
      spec.addMethod(tryParseOptionMethod())
          .addMethod(privateConstructor());
      spec.addField(commonFields.optionNames());
      spec.addField(commonFields.optionParsers());
    }
    if (!positionalParameters.regular().isEmpty()) {
      spec.addField(commonFields.params());
    }
    if (positionalParameters.anyRepeatable() || sourceElement.isSuperCommand()) {
      spec.addField(commonFields.rest());
    }
    spec.addMethod(buildMethod.get());
    return spec.build();
  }

  private MethodSpec privateConstructor() {
    CodeBlock.Builder code = CodeBlock.builder();
    for (ConvertedParameter<NamedOption> namedOption : namedOptions.options()) {
      for (String dashedName : namedOption.parameter().names()) {
        code.addStatement("$N.put($S, $T.$L)", commonFields.optionNames(), dashedName, sourceElement.optionType(),
            namedOption.enumConstant());
      }
      String enumConstant = namedOption.enumConstant();
      code.addStatement("$N.put($T.$L, new $T($T.$L))",
          commonFields.optionParsers(), sourceElement.optionType(), enumConstant, optionParserType(namedOption),
          sourceElement.optionType(), enumConstant);
    }
    return MethodSpec.constructorBuilder()
        .addCode(code.build())
        .build();
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

  private MethodSpec tryParseOptionMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    return MethodSpec.methodBuilder("tryParseOption")
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
    ParameterSpec option = ParameterSpec.builder(sourceElement.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $N.get($N($N))", sourceElement.optionType(),
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
        .addStatement("throw new $T($S + $N)", RuntimeException.class, "Invalid token: ", token)
        .unindent();
    code.endControlFlow();
    code.addStatement("return true");
    return code.build();
  }

  private CodeBlock tryParseOptionCodeSimple(ParameterSpec token, ParameterSpec it) {
    ParameterSpec option = ParameterSpec.builder(sourceElement.optionType(), "option").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $N.get($N($N))", sourceElement.optionType(), option,
        commonFields.optionNames(), readOptionNameMethod.get(), token);
    code.add("if ($N == null)\n", option).indent()
        .addStatement("return false")
        .unindent();
    code.addStatement("$N.get($N).read($N, $N)", commonFields.optionParsers(), option, token, it)
        .addStatement("return true");
    return code.build();
  }
}
