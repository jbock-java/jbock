package net.jbock.compiler;

import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionProvider;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

class PositionalParamFactory extends ParameterFactory {

  @Inject
  PositionalParamFactory(ParameterContext parameterContext) {
    super(parameterContext);
  }

  Parameter createPositionalParam(int positionalIndex) {
    parameterContext.checkBundleKey();
    Coercion coercion = CoercionProvider.nonFlagCoercion(sourceMethod(), sourceElement(), enumName(),
        mapperClass(), optionType(), tool());
    return new Parameter(' ', null, sourceMethod(), bundleKey(), enumName().snake().toLowerCase(Locale.US),
        Collections.emptyList(), coercion, Arrays.asList(description()), positionalIndex);
  }
}
