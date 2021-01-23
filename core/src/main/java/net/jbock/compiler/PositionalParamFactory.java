package net.jbock.compiler;

import net.jbock.coerce.BasicInfo;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.either.Either;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

class PositionalParamFactory extends ParameterScoped {

  private final BasicInfo basicInfo;

  @Inject
  PositionalParamFactory(ParameterContext parameterContext, BasicInfo basicInfo) {
    super(parameterContext);
    this.basicInfo = basicInfo;
  }

  Either<ValidationFailure, ? extends Parameter> createPositionalParam(int positionalIndex) {
    return Either.<String, Void>fromOptionalFailure(checkBundleKey())
        .select(() -> basicInfo.coercion()
            .map(coercion -> new PositionalParameter(sourceMethod(), bundleKey(), enumName().snake().toLowerCase(Locale.US),
                Collections.emptyList(), coercion, Arrays.asList(description()), positionalIndex)))
        .mapLeft(s -> new ValidationFailure(s, sourceMethod()));
  }
}
