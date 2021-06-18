package net.jbock.context;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.convert.Mapped;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;

import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class UsageMethod extends CachedMethod {

  private final PositionalParameters positionalParameters;
  private final NamedOptions namedOptions;
  private final SourceElement sourceElement;

  @Inject
  UsageMethod(
      PositionalParameters positionalParameters,
      NamedOptions namedOptions,
      SourceElement sourceElement) {
    this.positionalParameters = positionalParameters;
    this.namedOptions = namedOptions;
    this.sourceElement = sourceElement;
  }

  @Override
  MethodSpec define() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("usage");

    ParameterSpec result = builder(LIST_OF_STRING, "result").build();
    ParameterSpec prefix = builder(STRING, "prefix").build();

    spec.addStatement("$T $N = new $T<>()", result.type, result, ArrayList.class);
    spec.addStatement("$N.add($N)", result, prefix);
    spec.addStatement("$N.add($S)", result, sourceElement.programName());

    if (!namedOptions.optional().isEmpty()) {
      spec.addStatement("$N.add($S)", result, "[OPTION]...");
    }

    for (Mapped<NamedOption> option : namedOptions.required()) {
      String firstName = option.item().names().get(0);
      spec.addStatement("$N.add($T.format($S, $S, $S))",
          result, STRING, "%s %s",
          firstName,
          option.paramLabel());
    }

    for (Mapped<PositionalParameter> param : positionalParameters.regular()) {
      Multiplicity multiplicity = param.multiplicity();
      String paramLabel = param.paramLabel();
      switch (multiplicity) {
        case OPTIONAL:
          spec.addStatement("$N.add($S)", result, "[" + paramLabel + "]");
          break;
        case REQUIRED:
          spec.addStatement("$N.add($S)", result, paramLabel);
          break;
        default:
          throw new AssertionError("unexpected multiplicity: " + multiplicity);
      }
    }

    positionalParameters.repeatable().ifPresent(param ->
        spec.addStatement("$N.add($S)", result, "[" + param.paramLabel() + "]..."));

    spec.addStatement("return $N", result);
    return spec.returns(LIST_OF_STRING)
        .addModifiers(PRIVATE)
        .addParameter(prefix)
        .build();
  }
}
