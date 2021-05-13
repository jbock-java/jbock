package net.jbock.compiler.view;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Locale;

import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;

class UsageMethod {

  private final PositionalParameters positionalParameters;
  private final NamedOptions namedOptions;
  private final CommonFields commonFields;

  @Inject
  UsageMethod(
      PositionalParameters positionalParameters,
      NamedOptions namedOptions,
      CommonFields commonFields) {
    this.positionalParameters = positionalParameters;
    this.namedOptions = namedOptions;
    this.commonFields = commonFields;
  }

  MethodSpec define() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("usage");

    ParameterSpec result = builder(LIST_OF_STRING, "result").build();

    spec.addStatement("$T $N = new $T<>()", result.type, result, ArrayList.class);
    spec.addStatement("$N.add($S)", result, " ");
    spec.addStatement("$N.add($N)", result, commonFields.programName());

    if (!namedOptions.optional().isEmpty()) {
      spec.addStatement("$N.add($S)", result, "[OPTION]...");
    }

    for (ConvertedParameter<NamedOption> option : namedOptions.required()) {
      spec.addStatement("$N.add($T.format($S, $S, $S))",
          result, STRING, "%s %s",
          option.parameter().names().get(0),
          option.parameter().paramLabel());
    }

    for (ConvertedParameter<PositionalParameter> param : positionalParameters.regular()) {
      if (param.isOptional()) {
        spec.addStatement("$N.add($S)", result, "[" + param.enumName().snake().toUpperCase(Locale.US) + "]");
      } else if (param.isRequired()) {
        spec.addStatement("$N.add($S)", result, param.enumName().snake().toUpperCase(Locale.US));
      } else {
        throw new AssertionError("all cases handled (param can't be flag)");
      }
    }

    positionalParameters.repeatable().ifPresent(param ->
        spec.addStatement("$N.add($S)", result, "[" + param.enumName().snake().toUpperCase(Locale.US) + "]..."));

    spec.addStatement("return $N", result);
    return spec.returns(LIST_OF_STRING).addModifiers(PRIVATE).build();
  }
}
